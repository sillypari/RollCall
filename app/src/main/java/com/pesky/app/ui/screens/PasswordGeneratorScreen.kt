package com.pesky.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pesky.app.ui.components.CopyButton
import com.pesky.app.ui.components.PasswordStrengthMeter
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.utils.PasswordGenerator
import com.pesky.app.utils.PasswordGeneratorOptions
import com.pesky.app.utils.PasswordStrengthAnalyzer

/**
 * Password generator screen as a modal dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorDialog(
    onDismiss: () -> Unit,
    onPasswordSelected: (String) -> Unit
) {
    val generator = remember { PasswordGenerator() }
    val analyzer = remember { PasswordStrengthAnalyzer() }
    
    var options by remember { 
        mutableStateOf(PasswordGeneratorOptions())
    }
    
    var generatedPassword by remember { 
        mutableStateOf(generator.generate(options))
    }
    
    val passwordAnalysis = remember(generatedPassword) {
        analyzer.analyze(generatedPassword)
    }
    
    // Regenerate password when options change
    LaunchedEffect(options) {
        generatedPassword = generator.generate(options)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = PeskyColors.CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Password Generator",
                        style = MaterialTheme.typography.headlineSmall,
                        color = PeskyColors.TextPrimary
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            "Close",
                            tint = PeskyColors.IconSecondary
                        )
                    }
                }
                
                // Generated password display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PeskyColors.BackgroundPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = generatedPassword,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = PeskyColors.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = { 
                                generatedPassword = generator.generate(options) 
                            }
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                "Regenerate",
                                tint = PeskyColors.AccentBlue
                            )
                        }
                        
                        CopyButton(
                            onClick = { /* TODO: Copy and show feedback */ }
                        )
                    }
                }
                
                // Strength meter
                PasswordStrengthMeter(
                    analysisResult = passwordAnalysis,
                    showFeedback = false
                )
                
                Divider(color = PeskyColors.Divider)
                
                // Password length slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Length",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PeskyColors.TextPrimary
                        )
                        Text(
                            text = "${options.length} characters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PeskyColors.AccentBlue
                        )
                    }
                    
                    Slider(
                        value = options.length.toFloat(),
                        onValueChange = { 
                            options = options.copy(length = it.toInt()) 
                        },
                        valueRange = 8f..64f,
                        steps = 55,
                        colors = SliderDefaults.colors(
                            thumbColor = PeskyColors.AccentBlue,
                            activeTrackColor = PeskyColors.AccentBlue
                        )
                    )
                }
                
                // Character options
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OptionRow(
                        text = "Uppercase letters (A-Z)",
                        checked = options.includeUppercase,
                        onCheckedChange = { 
                            options = options.copy(includeUppercase = it) 
                        }
                    )
                    
                    OptionRow(
                        text = "Lowercase letters (a-z)",
                        checked = options.includeLowercase,
                        onCheckedChange = { 
                            options = options.copy(includeLowercase = it) 
                        }
                    )
                    
                    OptionRow(
                        text = "Numbers (0-9)",
                        checked = options.includeNumbers,
                        onCheckedChange = { 
                            options = options.copy(includeNumbers = it) 
                        }
                    )
                    
                    OptionRow(
                        text = "Symbols (!@#\$%...)",
                        checked = options.includeSymbols,
                        onCheckedChange = { 
                            options = options.copy(includeSymbols = it) 
                        }
                    )
                }
                
                Divider(color = PeskyColors.Divider)
                
                // Advanced options
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OptionRow(
                        text = "Exclude ambiguous characters (0O1lI)",
                        checked = options.excludeAmbiguous,
                        onCheckedChange = { 
                            options = options.copy(excludeAmbiguous = it) 
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PeskyColors.TextSecondary
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onPasswordSelected(generatedPassword) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PeskyColors.AccentBlue
                        )
                    ) {
                        Text("Use Password")
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PeskyColors.AccentBlue
            )
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = PeskyColors.TextPrimary
        )
    }
}
