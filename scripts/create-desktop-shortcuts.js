#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');
const util = require('util');

const execAsync = util.promisify(exec);

console.log('🖥️  Creating Gynassist Desktop Shortcuts');
console.log('=====================================');

const projectRoot = path.join(__dirname, '..');
const isWindows = process.platform === 'win32';
const isMac = process.platform === 'darwin';
const isLinux = process.platform === 'linux';

// Icon configurations
const iconConfig = {
  name: 'Gynassist',
  description: 'Reproductive Health Companion for Women',
  version: '1.0.0',
  author: 'Gynassist Team',
  website: 'https://gynassist.ug',
  categories: ['Health', 'Medical', 'Utility'],
  keywords: ['health', 'reproductive', 'women', 'medical', 'uganda']
};

async function createWindowsShortcut() {
  console.log('\n🪟 Creating Windows desktop shortcut...');
  
  const shortcutScript = `
$WshShell = New-Object -comObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut("$env:USERPROFILE\\Desktop\\Gynassist.lnk")
$Shortcut.TargetPath = "cmd.exe"
$Shortcut.Arguments = "/c cd /d \\"${projectRoot}\\" && npm run dev"
$Shortcut.WorkingDirectory = "${projectRoot}"
$Shortcut.IconLocation = "${path.join(projectRoot, 'assets', 'icon.ico')}"
$Shortcut.Description = "${iconConfig.description}"
$Shortcut.WindowStyle = 1
$Shortcut.Save()
`;

  const scriptPath = path.join(projectRoot, 'create-shortcut.ps1');
  fs.writeFileSync(scriptPath, shortcutScript);
  
  try {
    await execAsync(`powershell -ExecutionPolicy Bypass -File "${scriptPath}"`, { shell: true });
    console.log('✅ Windows desktop shortcut created');
    
    // Clean up script file
    fs.unlinkSync(scriptPath);
    
    // Also create Start Menu shortcut
    const startMenuScript = `
$WshShell = New-Object -comObject WScript.Shell
$StartMenuPath = "$env:APPDATA\\Microsoft\\Windows\\Start Menu\\Programs"
$Shortcut = $WshShell.CreateShortcut("$StartMenuPath\\Gynassist.lnk")
$Shortcut.TargetPath = "cmd.exe"
$Shortcut.Arguments = "/c cd /d \\"${projectRoot}\\" && npm run dev"
$Shortcut.WorkingDirectory = "${projectRoot}"
$Shortcut.IconLocation = "${path.join(projectRoot, 'assets', 'icon.ico')}"
$Shortcut.Description = "${iconConfig.description}"
$Shortcut.WindowStyle = 1
$Shortcut.Save()
`;
    
    const startMenuScriptPath = path.join(projectRoot, 'create-startmenu.ps1');
    fs.writeFileSync(startMenuScriptPath, startMenuScript);
    
    await execAsync(`powershell -ExecutionPolicy Bypass -File "${startMenuScriptPath}"`, { shell: true });
    console.log('✅ Windows Start Menu shortcut created');
    
    fs.unlinkSync(startMenuScriptPath);
    
  } catch (error) {
    console.error('❌ Failed to create Windows shortcuts:', error.message);
  }
}

async function createMacShortcut() {
  console.log('\n🍎 Creating macOS application bundle...');
  
  const appName = 'Gynassist.app';
  const appPath = path.join(process.env.HOME, 'Desktop', appName);
  const contentsPath = path.join(appPath, 'Contents');
  const macOSPath = path.join(contentsPath, 'MacOS');
  const resourcesPath = path.join(contentsPath, 'Resources');
  
  // Create directory structure
  fs.mkdirSync(macOSPath, { recursive: true });
  fs.mkdirSync(resourcesPath, { recursive: true });
  
  // Create Info.plist
  const infoPlist = `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>gynassist</string>
    <key>CFBundleIdentifier</key>
    <string>com.gynassist.app</string>
    <key>CFBundleName</key>
    <string>${iconConfig.name}</string>
    <key>CFBundleDisplayName</key>
    <string>${iconConfig.name}</string>
    <key>CFBundleVersion</key>
    <string>${iconConfig.version}</string>
    <key>CFBundleShortVersionString</key>
    <string>${iconConfig.version}</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleSignature</key>
    <string>GYNA</string>
    <key>CFBundleIconFile</key>
    <string>icon.icns</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.15</string>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>LSApplicationCategoryType</key>
    <string>public.app-category.healthcare-fitness</string>
</dict>
</plist>`;
  
  fs.writeFileSync(path.join(contentsPath, 'Info.plist'), infoPlist);
  
  // Create executable script
  const executable = `#!/bin/bash
cd "${projectRoot}"
npm run dev
`;
  
  const executablePath = path.join(macOSPath, 'gynassist');
  fs.writeFileSync(executablePath, executable);
  fs.chmodSync(executablePath, '755');
  
  // Copy icon if it exists
  const iconSource = path.join(projectRoot, 'assets', 'icon.icns');
  const iconDest = path.join(resourcesPath, 'icon.icns');
  
  if (fs.existsSync(iconSource)) {
    fs.copyFileSync(iconSource, iconDest);
  }
  
  console.log('✅ macOS application bundle created');
  console.log(`📁 Location: ${appPath}`);
}

async function createLinuxDesktopFile() {
  console.log('\n🐧 Creating Linux desktop file...');
  
  const desktopFile = `[Desktop Entry]
Version=1.0
Type=Application
Name=${iconConfig.name}
Comment=${iconConfig.description}
Exec=bash -c "cd '${projectRoot}' && npm run dev"
Icon=${path.join(projectRoot, 'assets', 'icon.png')}
Terminal=true
Categories=${iconConfig.categories.join(';')};
Keywords=${iconConfig.keywords.join(';')};
StartupNotify=true
StartupWMClass=gynassist
MimeType=application/x-gynassist;
`;

  // Create desktop file in user's desktop
  const desktopPath = path.join(process.env.HOME, 'Desktop', 'gynassist.desktop');
  fs.writeFileSync(desktopPath, desktopFile);
  fs.chmodSync(desktopPath, '755');
  
  // Also create in applications directory
  const applicationsDir = path.join(process.env.HOME, '.local', 'share', 'applications');
  if (!fs.existsSync(applicationsDir)) {
    fs.mkdirSync(applicationsDir, { recursive: true });
  }
  
  const appDesktopPath = path.join(applicationsDir, 'gynassist.desktop');
  fs.writeFileSync(appDesktopPath, desktopFile);
  fs.chmodSync(appDesktopPath, '755');
  
  console.log('✅ Linux desktop file created');
  console.log(`📁 Desktop: ${desktopPath}`);
  console.log(`📁 Applications: ${appDesktopPath}`);
}

async function createWebAppManifest() {
  console.log('\n🌐 Creating Progressive Web App manifest...');
  
  const manifest = {
    name: iconConfig.name,
    short_name: iconConfig.name,
    description: iconConfig.description,
    start_url: "/",
    display: "standalone",
    background_color: "#ffffff",
    theme_color: "#E91E63",
    orientation: "portrait-primary",
    categories: iconConfig.categories,
    icons: [
      {
        src: "/icon-192.png",
        sizes: "192x192",
        type: "image/png",
        purpose: "any maskable"
      },
      {
        src: "/icon-512.png", 
        sizes: "512x512",
        type: "image/png",
        purpose: "any maskable"
      }
    ],
    shortcuts: [
      {
        name: "AI Health Chat",
        short_name: "Chat",
        description: "Chat with AI health assistant",
        url: "/chat",
        icons: [{ src: "/chat-icon.png", sizes: "96x96" }]
      },
      {
        name: "Cycle Tracker",
        short_name: "Cycle",
        description: "Track menstrual cycle",
        url: "/cycle-tracker", 
        icons: [{ src: "/cycle-icon.png", sizes: "96x96" }]
      },
      {
        name: "Emergency Help",
        short_name: "Emergency",
        description: "Get emergency health guidance",
        url: "/emergency",
        icons: [{ src: "/emergency-icon.png", sizes: "96x96" }]
      }
    ]
  };
  
  const manifestPath = path.join(projectRoot, 'gynassist-frontend', 'public', 'manifest.json');
  fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 2));
  
  console.log('✅ PWA manifest created');
  console.log(`📁 Location: ${manifestPath}`);
}

async function createBatchFiles() {
  console.log('\n📝 Creating launcher scripts...');
  
  // Windows batch file
  if (isWindows) {
    const batchContent = `@echo off
title Gynassist - Reproductive Health App
echo Starting Gynassist...
echo.
cd /d "${projectRoot}"
npm run dev
pause
`;
    
    const batchPath = path.join(projectRoot, 'start-gynassist.bat');
    fs.writeFileSync(batchPath, batchContent);
    console.log('✅ Windows batch file created: start-gynassist.bat');
  }
  
  // Shell script for Unix-like systems
  if (isMac || isLinux) {
    const shellContent = `#!/bin/bash
echo "Starting Gynassist - Reproductive Health App"
echo "==========================================="
cd "${projectRoot}"
npm run dev
`;
    
    const shellPath = path.join(projectRoot, 'start-gynassist.sh');
    fs.writeFileSync(shellPath, shellContent);
    fs.chmodSync(shellPath, '755');
    console.log('✅ Shell script created: start-gynassist.sh');
  }
  
  // PowerShell script
  const psContent = `# Gynassist Launcher
Write-Host "Starting Gynassist - Reproductive Health App" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

Set-Location "${projectRoot}"

# Check if Node.js is installed
try {
    $nodeVersion = node --version
    Write-Host "Node.js version: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "Error: Node.js is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Node.js from https://nodejs.org/" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if dependencies are installed
if (!(Test-Path "node_modules")) {
    Write-Host "Installing dependencies..." -ForegroundColor Yellow
    npm install
}

# Start the application
Write-Host "Launching Gynassist..." -ForegroundColor Green
npm run dev
`;
  
  const psPath = path.join(projectRoot, 'start-gynassist.ps1');
  fs.writeFileSync(psPath, psContent);
  console.log('✅ PowerShell script created: start-gynassist.ps1');
}

async function createDefaultIcons() {
  console.log('\n🎨 Creating default application icons...');
  
  const assetsDir = path.join(projectRoot, 'assets');
  if (!fs.existsSync(assetsDir)) {
    fs.mkdirSync(assetsDir, { recursive: true });
  }
  
  // Create a simple SVG icon as placeholder
  const svgIcon = `<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:#E91E63;stop-opacity:1" />
      <stop offset="100%" style="stop-color:#9C27B0;stop-opacity:1" />
    </linearGradient>
  </defs>
  <circle cx="256" cy="256" r="240" fill="url(#grad1)" stroke="#fff" stroke-width="8"/>
  <path d="M256 120c-75 0-136 61-136 136s61 136 136 136 136-61 136-136-61-136-136-136zm0 240c-57 0-104-47-104-104s47-104 104-104 104 47 104 104-47 104-104 104z" fill="#fff"/>
  <circle cx="256" cy="256" r="40" fill="#fff"/>
  <text x="256" y="420" font-family="Arial, sans-serif" font-size="48" font-weight="bold" text-anchor="middle" fill="#fff">G</text>
</svg>`;
  
  fs.writeFileSync(path.join(assetsDir, 'icon.svg'), svgIcon);
  
  // Create placeholder files for different formats
  const placeholderFiles = [
    'icon.png',
    'icon.ico', 
    'icon.icns',
    'icon-192.png',
    'icon-512.png',
    'tray-icon.png',
    'notification-icon.png'
  ];
  
  placeholderFiles.forEach(filename => {
    const filePath = path.join(assetsDir, filename);
    if (!fs.existsSync(filePath)) {
      // Create a simple text file as placeholder
      fs.writeFileSync(filePath, `# Placeholder for ${filename}\n# Replace with actual icon file`);
    }
  });
  
  console.log('✅ Default icon files created in assets/ directory');
  console.log('💡 Replace placeholder files with actual icons for better appearance');
}

async function updatePackageJsonScripts() {
  console.log('\n📦 Updating package.json with launcher scripts...');
  
  const packageJsonPath = path.join(projectRoot, 'package.json');
  
  if (fs.existsSync(packageJsonPath)) {
    const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
    
    // Add launcher scripts
    packageJson.scripts = {
      ...packageJson.scripts,
      "launch": "node scripts/create-desktop-shortcuts.js",
      "launch:windows": "start-gynassist.bat",
      "launch:mac": "./start-gynassist.sh",
      "launch:linux": "./start-gynassist.sh",
      "launch:powershell": "powershell -ExecutionPolicy Bypass -File start-gynassist.ps1"
    };
    
    fs.writeFileSync(packageJsonPath, JSON.stringify(packageJson, null, 2));
    console.log('✅ Package.json updated with launcher scripts');
  }
}

async function createInstallationInstructions() {
  console.log('\n📋 Creating installation instructions...');
  
  const instructions = `# Gynassist Desktop Launcher Setup

## 🚀 Quick Launch Options

### Option 1: Desktop Shortcut
- **Windows:** Double-click "Gynassist" shortcut on desktop
- **macOS:** Double-click "Gynassist.app" on desktop  
- **Linux:** Double-click "gynassist.desktop" on desktop

### Option 2: Command Line
\`\`\`bash
# From project directory
npm run dev

# Or use platform-specific launchers
npm run launch:windows    # Windows batch file
npm run launch:mac        # macOS/Linux shell script  
npm run launch:powershell # PowerShell script
\`\`\`

### Option 3: Direct Script Execution
- **Windows:** Double-click \`start-gynassist.bat\`
- **macOS/Linux:** Run \`./start-gynassist.sh\`
- **PowerShell:** Run \`start-gynassist.ps1\`

## 🎨 Custom Icon Setup

To use your own icon:

1. **Prepare icon files:**
   - \`icon.png\` (512x512) - General use
   - \`icon.ico\` (Windows) - Multiple sizes embedded
   - \`icon.icns\` (macOS) - Apple icon format
   - \`icon-192.png\` & \`icon-512.png\` - PWA icons

2. **Replace placeholder files in \`assets/\` directory**

3. **Regenerate shortcuts:**
   \`\`\`bash
   npm run launch
   \`\`\`

## 🌐 Progressive Web App (PWA)

Install as web app:
1. Open http://localhost:5173 in Chrome/Edge
2. Click install button in address bar
3. App appears in applications menu

## 🔧 Troubleshooting

### Shortcut doesn't work
- Verify Node.js is installed: \`node --version\`
- Check project path in shortcut properties
- Run \`npm install\` in project directory

### Icon doesn't appear
- Ensure icon files exist in \`assets/\` directory
- Use proper format for your platform
- Regenerate shortcuts after adding icons

### Permission errors (Linux/macOS)
\`\`\`bash
chmod +x start-gynassist.sh
chmod +x ~/.local/share/applications/gynassist.desktop
\`\`\`

## 📱 Mobile & Desktop Apps

For native mobile and desktop apps:
- **Mobile:** \`npm run start:mobile\` (Expo)
- **Desktop:** \`npm run start:desktop\` (Electron)

---

**Gynassist** - Empowering women's reproductive health 🌸
`;

  fs.writeFileSync(path.join(projectRoot, 'LAUNCHER.md'), instructions);
  console.log('✅ Launcher instructions created: LAUNCHER.md');
}

async function main() {
  console.log('🎯 Setting up desktop launcher for Gynassist...\n');
  
  try {
    // Create default icons
    await createDefaultIcons();
    
    // Create launcher scripts
    await createBatchFiles();
    
    // Create PWA manifest
    await createWebAppManifest();
    
    // Create platform-specific shortcuts
    if (isWindows) {
      await createWindowsShortcut();
    } else if (isMac) {
      await createMacShortcut();
    } else if (isLinux) {
      await createLinuxDesktopFile();
    }
    
    // Update package.json
    await updatePackageJsonScripts();
    
    // Create documentation
    await createInstallationInstructions();
    
    console.log('\n' + '='.repeat(60));
    console.log('🎉 Desktop Launcher Setup Complete!');
    console.log('='.repeat(60));
    
    console.log('\n📋 What was created:');
    console.log('   ✅ Desktop shortcuts (platform-specific)');
    console.log('   ✅ Launcher scripts (.bat, .sh, .ps1)');
    console.log('   ✅ PWA manifest for web installation');
    console.log('   ✅ Default icon placeholders');
    console.log('   ✅ Installation documentation');
    
    console.log('\n🚀 How to launch Gynassist:');
    if (isWindows) {
      console.log('   • Double-click desktop shortcut');
      console.log('   • Run: start-gynassist.bat');
      console.log('   • Start Menu → Gynassist');
    } else if (isMac) {
      console.log('   • Double-click Gynassist.app on desktop');
      console.log('   • Run: ./start-gynassist.sh');
    } else if (isLinux) {
      console.log('   • Double-click desktop file');
      console.log('   • Applications menu → Gynassist');
      console.log('   • Run: ./start-gynassist.sh');
    }
    
    console.log('\n🎨 Next steps:');
    console.log('   1. Replace placeholder icons in assets/ with your design');
    console.log('   2. Test the launcher by double-clicking the shortcut');
    console.log('   3. Customize launcher scripts if needed');
    console.log('   4. Read LAUNCHER.md for detailed instructions');
    
    console.log('\n💡 Pro tip: Run "npm run launch" to regenerate shortcuts anytime');
    
  } catch (error) {
    console.error('\n❌ Setup failed:', error.message);
    process.exit(1);
  }
}

// Run the setup
main().catch(error => {
  console.error('❌ Launcher setup failed:', error.message);
  process.exit(1);
});