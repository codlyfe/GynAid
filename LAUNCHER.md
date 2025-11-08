# Gynassist Desktop Launcher Setup

## 🚀 Quick Launch Options

### Option 1: Desktop Shortcut
- **Windows:** Double-click "Gynassist" shortcut on desktop
- **macOS:** Double-click "Gynassist.app" on desktop  
- **Linux:** Double-click "gynassist.desktop" on desktop

### Option 2: Command Line
```bash
# From project directory
npm run dev

# Or use platform-specific launchers
npm run launch:windows    # Windows batch file
npm run launch:mac        # macOS/Linux shell script  
npm run launch:powershell # PowerShell script
```

### Option 3: Direct Script Execution
- **Windows:** Double-click `start-gynassist.bat`
- **macOS/Linux:** Run `./start-gynassist.sh`
- **PowerShell:** Run `start-gynassist.ps1`

## 🎨 Custom Icon Setup

To use your own icon:

1. **Prepare icon files:**
   - `icon.png` (512x512) - General use
   - `icon.ico` (Windows) - Multiple sizes embedded
   - `icon.icns` (macOS) - Apple icon format
   - `icon-192.png` & `icon-512.png` - PWA icons

2. **Replace placeholder files in `assets/` directory**

3. **Regenerate shortcuts:**
   ```bash
   npm run launch
   ```

## 🌐 Progressive Web App (PWA)

Install as web app:
1. Open http://localhost:5173 in Chrome/Edge
2. Click install button in address bar
3. App appears in applications menu

## 🔧 Troubleshooting

### Shortcut doesn't work
- Verify Node.js is installed: `node --version`
- Check project path in shortcut properties
- Run `npm install` in project directory

### Icon doesn't appear
- Ensure icon files exist in `assets/` directory
- Use proper format for your platform
- Regenerate shortcuts after adding icons

### Permission errors (Linux/macOS)
```bash
chmod +x start-gynassist.sh
chmod +x ~/.local/share/applications/gynassist.desktop
```

## 📱 Mobile & Desktop Apps

For native mobile and desktop apps:
- **Mobile:** `npm run start:mobile` (Expo)
- **Desktop:** `npm run start:desktop` (Electron)

---

**Gynassist** - Empowering women's reproductive health 🌸
