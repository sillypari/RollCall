package com.pesky.app.data.database

import com.pesky.app.data.models.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Parses and generates XML content for the encrypted database payload.
 */
@Singleton
class XMLParser @Inject constructor() {
    
    companion object {
        private val dateFormatter = DateTimeFormatter.ISO_INSTANT
    }
    
    /**
     * Parses XML bytes into a VaultDatabase.
     */
    fun parseXML(xmlBytes: ByteArray): Result<VaultDatabase> {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = false
            
            val builder = factory.newDocumentBuilder()
            val inputStream = ByteArrayInputStream(xmlBytes)
            val document = builder.parse(inputStream)
            document.documentElement.normalize()
            
            val root = document.documentElement
            if (root.tagName != "Database") {
                return Result.failure(XMLParseException("Invalid root element: ${root.tagName}"))
            }
            
            val metadata = parseMetadata(root.getChildElement("Metadata"))
            val rootElement = root.getChildElement("Root")
            
            val groups = mutableListOf<Group>()
            val entries = mutableListOf<PasswordEntry>()
            val deletedObjects = mutableListOf<DeletedObject>()
            
            // Parse groups and entries
            rootElement?.let { rootEl ->
                parseGroups(rootEl, null, groups, entries)
                
                // Parse deleted objects
                rootEl.getChildElement("DeletedObjects")?.let { deletedEl ->
                    parseDeletedObjects(deletedEl, deletedObjects)
                }
            }
            
            Result.success(
                VaultDatabase(
                    metadata = metadata,
                    groups = groups.ifEmpty { listOf(Group.ROOT) },
                    entries = entries,
                    deletedObjects = deletedObjects
                )
            )
        } catch (e: Exception) {
            Result.failure(XMLParseException("Failed to parse XML: ${e.message}", e))
        }
    }
    
    /**
     * Generates XML bytes from a VaultDatabase.
     */
    fun generateXML(database: VaultDatabase): ByteArray {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.newDocument()
        
        // Root element
        val root = document.createElement("Database")
        document.appendChild(root)
        
        // Metadata
        val metadataEl = createMetadataElement(document, database.metadata)
        root.appendChild(metadataEl)
        
        // Root group
        val rootGroupEl = document.createElement("Root")
        root.appendChild(rootGroupEl)
        
        // Add groups and entries
        for (group in database.groups.filter { it.uuid != "root" }) {
            val groupEl = createGroupElement(document, group, database.entries)
            rootGroupEl.appendChild(groupEl)
        }
        
        // Add entries without group to root
        for (entry in database.entries.filter { it.groupUuid == null }) {
            val entryEl = createEntryElement(document, entry)
            rootGroupEl.appendChild(entryEl)
        }
        
        // Deleted objects
        val deletedEl = document.createElement("DeletedObjects")
        for (deleted in database.deletedObjects) {
            val deletedObjEl = document.createElement("DeletedObject")
            deletedObjEl.setAttribute("UUID", deleted.uuid)
            
            val timeEl = document.createElement("DeletionTime")
            timeEl.textContent = formatInstant(deleted.deletionTime)
            deletedObjEl.appendChild(timeEl)
            
            deletedEl.appendChild(deletedObjEl)
        }
        rootGroupEl.appendChild(deletedEl)
        
        // Transform to string
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        
        val writer = StringWriter()
        transformer.transform(DOMSource(document), StreamResult(writer))
        
        return writer.toString().toByteArray(Charsets.UTF_8)
    }
    
    private fun parseMetadata(element: Element?): DatabaseMetadata {
        if (element == null) return DatabaseMetadata()
        
        return DatabaseMetadata(
            version = element.getChildText("Version") ?: "4.1.0",
            databaseName = element.getChildText("DatabaseName") ?: "My Passwords",
            databaseDescription = element.getChildText("DatabaseDescription") ?: "",
            creationTime = parseInstant(element.getChildText("CreationTime")),
            lastModificationTime = parseInstant(element.getChildText("LastModificationTime")),
            generator = element.getChildText("Generator") ?: "Pesky"
        )
    }
    
    private fun parseGroups(
        element: Element,
        parentUuid: String?,
        groups: MutableList<Group>,
        entries: MutableList<PasswordEntry>
    ) {
        val groupElements = element.getElementsByTagName("Group")
        
        for (i in 0 until groupElements.length) {
            val groupNode = groupElements.item(i)
            if (groupNode.nodeType == Node.ELEMENT_NODE && groupNode.parentNode == element) {
                val groupEl = groupNode as Element
                val group = parseGroup(groupEl, parentUuid)
                groups.add(group)
                
                // Parse entries in this group
                val entryElements = groupEl.getElementsByTagName("Entry")
                for (j in 0 until entryElements.length) {
                    val entryNode = entryElements.item(j)
                    if (entryNode.nodeType == Node.ELEMENT_NODE && entryNode.parentNode == groupEl) {
                        val entry = parseEntry(entryNode as Element, group.uuid)
                        entries.add(entry)
                    }
                }
                
                // Recursively parse nested groups
                parseGroups(groupEl, group.uuid, groups, entries)
            }
        }
    }
    
    private fun parseGroup(element: Element, parentUuid: String?): Group {
        val uuid = element.getAttribute("UUID").takeIf { it.isNotEmpty() } 
            ?: java.util.UUID.randomUUID().toString()
        
        val timesEl = element.getChildElement("Times")
        
        return Group(
            uuid = uuid,
            name = element.getChildText("Name") ?: "Unnamed",
            iconId = element.getChildText("IconID")?.toIntOrNull() ?: 0,
            parentUuid = parentUuid,
            times = GroupTimes(
                creationTime = parseInstant(timesEl?.getChildText("CreationTime")),
                lastModificationTime = parseInstant(timesEl?.getChildText("LastModificationTime"))
            )
        )
    }
    
    private fun parseEntry(element: Element, groupUuid: String?): PasswordEntry {
        val uuid = element.getAttribute("UUID").takeIf { it.isNotEmpty() }
            ?: java.util.UUID.randomUUID().toString()
        
        val timesEl = element.getChildElement("Times")
        val expiryTimeEl = timesEl?.getChildElement("ExpiryTime")
        
        val customFields = mutableListOf<CustomField>()
        element.getChildElement("CustomFields")?.let { fieldsEl ->
            val fieldElements = fieldsEl.getElementsByTagName("Field")
            for (i in 0 until fieldElements.length) {
                val fieldEl = fieldElements.item(i) as Element
                customFields.add(
                    CustomField(
                        key = fieldEl.getAttribute("Key"),
                        value = fieldEl.getAttribute("Value"),
                        isProtected = fieldEl.getAttribute("Protected") == "True"
                    )
                )
            }
        }
        
        val history = mutableListOf<PasswordHistoryEntry>()
        element.getChildElement("History")?.let { historyEl ->
            val historyEntries = historyEl.getElementsByTagName("HistoryEntry")
            for (i in 0 until historyEntries.length) {
                val histEl = historyEntries.item(i) as Element
                history.add(
                    PasswordHistoryEntry(
                        password = histEl.getChildText("Password") ?: "",
                        modificationTime = parseInstant(histEl.getChildText("ModificationTime"))
                    )
                )
            }
        }
        
        val tagsText = element.getChildText("Tags") ?: ""
        val tags = if (tagsText.isNotEmpty()) {
            tagsText.split(",").map { it.trim() }
        } else {
            emptyList()
        }
        
        return PasswordEntry(
            uuid = uuid,
            title = element.getChildText("Title") ?: "",
            userName = element.getChildText("UserName") ?: "",
            password = element.getChildText("Password") ?: "",
            url = element.getChildText("URL") ?: "",
            notes = element.getChildText("Notes") ?: "",
            tags = tags,
            iconId = element.getChildText("IconID")?.toIntOrNull() ?: 0,
            foregroundColor = element.getChildText("ForegroundColor"),
            groupUuid = groupUuid,
            customFields = customFields,
            history = history,
            times = EntryTimes(
                creationTime = parseInstant(timesEl?.getChildText("CreationTime")),
                lastModificationTime = parseInstant(timesEl?.getChildText("LastModificationTime")),
                lastAccessTime = parseInstant(timesEl?.getChildText("LastAccessTime")),
                expiryTime = expiryTimeEl?.textContent?.let { parseInstant(it) },
                expires = expiryTimeEl?.getAttribute("Expires") == "True"
            )
        )
    }
    
    private fun parseDeletedObjects(element: Element, deletedObjects: MutableList<DeletedObject>) {
        val deletedElements = element.getElementsByTagName("DeletedObject")
        for (i in 0 until deletedElements.length) {
            val deletedEl = deletedElements.item(i) as Element
            deletedObjects.add(
                DeletedObject(
                    uuid = deletedEl.getAttribute("UUID"),
                    deletionTime = parseInstant(deletedEl.getChildText("DeletionTime"))
                )
            )
        }
    }
    
    private fun createMetadataElement(document: Document, metadata: DatabaseMetadata): Element {
        val element = document.createElement("Metadata")
        
        element.appendChildWithText(document, "Version", metadata.version)
        element.appendChildWithText(document, "DatabaseName", metadata.databaseName)
        element.appendChildWithText(document, "DatabaseDescription", metadata.databaseDescription)
        element.appendChildWithText(document, "CreationTime", formatInstant(metadata.creationTime))
        element.appendChildWithText(document, "LastModificationTime", formatInstant(metadata.lastModificationTime))
        element.appendChildWithText(document, "Generator", metadata.generator)
        
        return element
    }
    
    private fun createGroupElement(document: Document, group: Group, allEntries: List<PasswordEntry>): Element {
        val element = document.createElement("Group")
        element.setAttribute("UUID", group.uuid)
        
        element.appendChildWithText(document, "Name", group.name)
        element.appendChildWithText(document, "IconID", group.iconId.toString())
        
        val timesEl = document.createElement("Times")
        timesEl.appendChildWithText(document, "CreationTime", formatInstant(group.times.creationTime))
        timesEl.appendChildWithText(document, "LastModificationTime", formatInstant(group.times.lastModificationTime))
        element.appendChild(timesEl)
        
        // Add entries belonging to this group
        for (entry in allEntries.filter { it.groupUuid == group.uuid }) {
            element.appendChild(createEntryElement(document, entry))
        }
        
        return element
    }
    
    private fun createEntryElement(document: Document, entry: PasswordEntry): Element {
        val element = document.createElement("Entry")
        element.setAttribute("UUID", entry.uuid)
        
        element.appendChildWithText(document, "Title", entry.title)
        element.appendChildWithText(document, "UserName", entry.userName)
        
        val passwordEl = document.createElement("Password")
        passwordEl.setAttribute("Protected", "True")
        passwordEl.textContent = entry.password
        element.appendChild(passwordEl)
        
        element.appendChildWithText(document, "URL", entry.url)
        element.appendChildWithText(document, "Notes", entry.notes)
        element.appendChildWithText(document, "Tags", entry.tags.joinToString(","))
        element.appendChildWithText(document, "IconID", entry.iconId.toString())
        entry.foregroundColor?.let { element.appendChildWithText(document, "ForegroundColor", it) }
        
        // Times
        val timesEl = document.createElement("Times")
        timesEl.appendChildWithText(document, "CreationTime", formatInstant(entry.times.creationTime))
        timesEl.appendChildWithText(document, "LastModificationTime", formatInstant(entry.times.lastModificationTime))
        timesEl.appendChildWithText(document, "LastAccessTime", formatInstant(entry.times.lastAccessTime))
        
        if (entry.times.expires && entry.times.expiryTime != null) {
            val expiryEl = document.createElement("ExpiryTime")
            expiryEl.setAttribute("Expires", "True")
            expiryEl.textContent = formatInstant(entry.times.expiryTime)
            timesEl.appendChild(expiryEl)
        }
        element.appendChild(timesEl)
        
        // Custom fields
        if (entry.customFields.isNotEmpty()) {
            val fieldsEl = document.createElement("CustomFields")
            for (field in entry.customFields) {
                val fieldEl = document.createElement("Field")
                fieldEl.setAttribute("Key", field.key)
                fieldEl.setAttribute("Value", field.value)
                fieldEl.setAttribute("Protected", if (field.isProtected) "True" else "False")
                fieldsEl.appendChild(fieldEl)
            }
            element.appendChild(fieldsEl)
        }
        
        // History
        if (entry.history.isNotEmpty()) {
            val historyEl = document.createElement("History")
            for (histEntry in entry.history) {
                val histEntryEl = document.createElement("HistoryEntry")
                histEntryEl.appendChildWithText(document, "Password", histEntry.password)
                histEntryEl.appendChildWithText(document, "ModificationTime", formatInstant(histEntry.modificationTime))
                historyEl.appendChild(histEntryEl)
            }
            element.appendChild(historyEl)
        }
        
        // AutoType
        val autoTypeEl = document.createElement("AutoType")
        autoTypeEl.appendChildWithText(document, "Enabled", if (entry.autoTypeEnabled) "True" else "False")
        element.appendChild(autoTypeEl)
        
        return element
    }
    
    // Extension functions
    private fun Element.getChildElement(tagName: String): Element? {
        val list = getElementsByTagName(tagName)
        for (i in 0 until list.length) {
            val node = list.item(i)
            if (node.parentNode == this) {
                return node as Element
            }
        }
        return null
    }
    
    private fun Element.getChildText(tagName: String): String? {
        return getChildElement(tagName)?.textContent
    }
    
    private fun Element.appendChildWithText(document: Document, tagName: String, text: String) {
        val child = document.createElement(tagName)
        child.textContent = text
        appendChild(child)
    }
    
    private fun parseInstant(text: String?): Instant {
        if (text.isNullOrEmpty()) return Instant.now()
        return try {
            Instant.parse(text)
        } catch (e: Exception) {
            Instant.now()
        }
    }
    
    private fun formatInstant(instant: Instant): String {
        return dateFormatter.format(instant)
    }
}

/**
 * Exception for XML parsing errors.
 */
class XMLParseException(message: String, cause: Throwable? = null) : Exception(message, cause)
