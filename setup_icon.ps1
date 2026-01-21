# Script to resize and copy app icon to all DPI folders
# Place your source image as 'pesky_logo.png' in the project root

$sourceImage = "pesky_logo.png"
$projectRoot = $PSScriptRoot

if (-not (Test-Path $sourceImage)) {
    Write-Host "Error: Please save your logo as 'pesky_logo.png' in the project root" -ForegroundColor Red
    exit 1
}

# Icon sizes for each DPI
$sizes = @{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

# Load image processing assembly
Add-Type -AssemblyName System.Drawing

# Load source image
$sourceImg = [System.Drawing.Image]::FromFile((Resolve-Path $sourceImage))

foreach ($folder in $sizes.Keys) {
    $size = $sizes[$folder]
    $targetFolder = Join-Path $projectRoot "app\src\main\res\$folder"
    
    # Create folder if it doesn't exist
    if (-not (Test-Path $targetFolder)) {
        New-Item -ItemType Directory -Path $targetFolder -Force | Out-Null
    }
    
    # Create resized image
    $resizedImg = New-Object System.Drawing.Bitmap($size, $size)
    $graphics = [System.Drawing.Graphics]::FromImage($resizedImg)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.DrawImage($sourceImg, 0, 0, $size, $size)
    
    # Save as ic_launcher.png
    $targetPath = Join-Path $targetFolder "ic_launcher.png"
    $resizedImg.Save($targetPath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    # Also save as ic_launcher_round.png
    $targetRoundPath = Join-Path $targetFolder "ic_launcher_round.png"
    $resizedImg.Save($targetRoundPath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    Write-Host "Created $($size)x$($size) icon in $folder" -ForegroundColor Green
    
    $graphics.Dispose()
    $resizedImg.Dispose()
}

$sourceImg.Dispose()
Write-Host "`nAll icons created successfully!" -ForegroundColor Green
Write-Host "Now rebuild your app to see the new icon." -ForegroundColor Cyan
