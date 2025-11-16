const { app, BrowserWindow, Menu, Tray, shell, ipcMain, dialog, Notification } = require('electron');
const path = require('path');

// Check if in development mode
let isDev = false;
app.whenReady().then(() => {
  isDev = process.env.NODE_ENV === 'development' || !app.isPackaged;
});
// Simple storage fallback
const store = {
  get: (key) => null,
  set: (key, value) => {},
  delete: (key) => {}
};

let mainWindow;
let tray;
let isQuitting = false;

// Enable live reload for Electron in development
if (isDev) {
  try {
    require('electron-reload')(__dirname, {
      electron: path.join(__dirname, '..', 'node_modules', '.bin', 'electron'),
      hardResetMethod: 'exit'
    });
  } catch (e) {
    console.log('Electron reload not available');
  }
}

function createWindow() {
  // Create the browser window
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 800,
    minHeight: 600,
    icon: path.join(__dirname, '../assets/icon.png'),
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      enableRemoteModule: false,
      preload: path.join(__dirname, 'preload.js'),
    },
    titleBarStyle: process.platform === 'darwin' ? 'hiddenInset' : 'default',
    show: false, // Don't show until ready
  });

  // Load the app
  const startUrl = isDev 
    ? 'http://localhost:5173' 
    : `file://${path.join(__dirname, '../build/index.html')}`;
  
  mainWindow.loadURL(startUrl);

  // Show window when ready
  mainWindow.once('ready-to-show', () => {
    mainWindow.show();
    
    // Focus on window
    if (isDev) {
      mainWindow.webContents.openDevTools();
    }
  });

  // Handle window closed
  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  // Handle window close (minimize to tray on Windows/Linux)
  mainWindow.on('close', (event) => {
    if (!isQuitting && process.platform !== 'darwin') {
      event.preventDefault();
      mainWindow.hide();
      
      // Show notification
      if (Notification.isSupported()) {
        new Notification({
          title: 'GynAid',
          body: 'App was minimized to tray',
          icon: path.join(__dirname, '../assets/icon.png')
        }).show();
      }
    }
  });

  // Handle external links
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });
}

function createTray() {
  const trayIcon = path.join(__dirname, '../assets/tray-icon.png');
  tray = new Tray(trayIcon);
  
  const contextMenu = Menu.buildFromTemplate([
    {
      label: 'Show GynAid',
      click: () => {
        mainWindow.show();
        mainWindow.focus();
      }
    },
    {
      label: 'Dashboard',
      click: () => {
        mainWindow.show();
        mainWindow.focus();
        mainWindow.webContents.send('navigate-to', '/dashboard');
      }
    },
    {
      label: 'AI Chat',
      click: () => {
        mainWindow.show();
        mainWindow.focus();
        mainWindow.webContents.send('navigate-to', '/chat');
      }
    },
    { type: 'separator' },
    {
      label: 'Emergency Help',
      click: () => {
        mainWindow.show();
        mainWindow.focus();
        mainWindow.webContents.send('navigate-to', '/emergency');
      }
    },
    { type: 'separator' },
    {
      label: 'Quit',
      click: () => {
        isQuitting = true;
        app.quit();
      }
    }
  ]);
  
  tray.setToolTip('GynAid - Reproductive Health Companion');
  tray.setContextMenu(contextMenu);
  
  // Double click to show window
  tray.on('double-click', () => {
    mainWindow.show();
    mainWindow.focus();
  });
}

function createMenu() {
  const template = [
    {
      label: 'File',
      submenu: [
        {
          label: 'New Cycle Entry',
          accelerator: 'CmdOrCtrl+N',
          click: () => {
            mainWindow.webContents.send('navigate-to', '/cycle-tracker');
          }
        },
        { type: 'separator' },
        {
          label: 'Export Data',
          accelerator: 'CmdOrCtrl+E',
          click: async () => {
            const result = await dialog.showSaveDialog(mainWindow, {
              defaultPath: 'GynAid-data.json',
              filters: [
                { name: 'JSON Files', extensions: ['json'] },
                { name: 'All Files', extensions: ['*'] }
              ]
            });
            
            if (!result.canceled) {
              mainWindow.webContents.send('export-data', result.filePath);
            }
          }
        },
        { type: 'separator' },
        process.platform === 'darwin' ? { role: 'close' } : { role: 'quit' }
      ]
    },
    {
      label: 'Health',
      submenu: [
        {
          label: 'AI Health Assistant',
          accelerator: 'CmdOrCtrl+H',
          click: () => {
            mainWindow.webContents.send('navigate-to', '/chat');
          }
        },
        {
          label: 'Cycle Tracker',
          accelerator: 'CmdOrCtrl+T',
          click: () => {
            mainWindow.webContents.send('navigate-to', '/cycle-tracker');
          }
        },
        {
          label: 'Book Consultation',
          accelerator: 'CmdOrCtrl+B',
          click: () => {
            mainWindow.webContents.send('navigate-to', '/consultations');
          }
        },
        { type: 'separator' },
        {
          label: 'Emergency Help',
          accelerator: 'CmdOrCtrl+Shift+E',
          click: () => {
            mainWindow.webContents.send('navigate-to', '/emergency');
          }
        }
      ]
    },
    {
      label: 'View',
      submenu: [
        { role: 'reload' },
        { role: 'forceReload' },
        { role: 'toggleDevTools' },
        { type: 'separator' },
        { role: 'resetZoom' },
        { role: 'zoomIn' },
        { role: 'zoomOut' },
        { type: 'separator' },
        { role: 'togglefullscreen' }
      ]
    },
    {
      label: 'Window',
      submenu: [
        { role: 'minimize' },
        { role: 'close' },
        {
          label: 'Always on Top',
          type: 'checkbox',
          click: (menuItem) => {
            mainWindow.setAlwaysOnTop(menuItem.checked);
          }
        }
      ]
    },
    {
      role: 'help',
      submenu: [
        {
          label: 'About GynAid',
          click: () => {
            dialog.showMessageBox(mainWindow, {
              type: 'info',
              title: 'About GynAid',
              message: 'GynAid Desktop',
              detail: 'Reproductive Health Companion for Women\nVersion 1.0.0\n\nEmpowering women\'s health across Uganda 🇺🇬'
            });
          }
        },
        {
          label: 'Learn More',
          click: () => {
            shell.openExternal('https://GynAid.ug');
          }
        }
      ]
    }
  ];

  if (process.platform === 'darwin') {
    template.unshift({
      label: app.getName(),
      submenu: [
        { role: 'about' },
        { type: 'separator' },
        { role: 'services' },
        { type: 'separator' },
        { role: 'hide' },
        { role: 'hideOthers' },
        { role: 'unhide' },
        { type: 'separator' },
        { role: 'quit' }
      ]
    });
  }

  const menu = Menu.buildFromTemplate(template);
  Menu.setApplicationMenu(menu);
}

// App event handlers
app.whenReady().then(() => {
  createWindow();
  createTray();
  createMenu();
  
  // Auto-updater disabled for simplicity
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  } else {
    mainWindow.show();
  }
});

app.on('before-quit', () => {
  isQuitting = true;
});

// IPC handlers
ipcMain.handle('get-app-version', () => {
  return app.getVersion();
});

ipcMain.handle('get-platform', () => {
  return process.platform;
});

ipcMain.handle('store-get', (event, key) => {
  return store.get(key);
});

ipcMain.handle('store-set', (event, key, value) => {
  store.set(key, value);
});

ipcMain.handle('store-delete', (event, key) => {
  store.delete(key);
});

ipcMain.handle('show-notification', (event, options) => {
  if (Notification.isSupported()) {
    const notification = new Notification({
      title: options.title,
      body: options.body,
      icon: path.join(__dirname, '../assets/icon.png'),
      ...options
    });
    
    notification.show();
    
    if (options.onClick) {
      notification.on('click', () => {
        mainWindow.show();
        mainWindow.focus();
      });
    }
  }
});

ipcMain.handle('open-external', (event, url) => {
  shell.openExternal(url);
});

// Auto-updater functionality removed for stability
