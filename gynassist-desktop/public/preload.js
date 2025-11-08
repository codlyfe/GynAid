const { contextBridge, ipcRenderer } = require('electron');

// Expose protected methods that allow the renderer process to use
// the ipcRenderer without exposing the entire object
contextBridge.exposeInMainWorld('electronAPI', {
  // App info
  getAppVersion: () => ipcRenderer.invoke('get-app-version'),
  getPlatform: () => ipcRenderer.invoke('get-platform'),
  
  // Storage
  store: {
    get: (key) => ipcRenderer.invoke('store-get', key),
    set: (key, value) => ipcRenderer.invoke('store-set', key, value),
    delete: (key) => ipcRenderer.invoke('store-delete', key),
  },
  
  // Notifications
  showNotification: (options) => ipcRenderer.invoke('show-notification', options),
  
  // External links
  openExternal: (url) => ipcRenderer.invoke('open-external', url),
  
  // Navigation
  onNavigateTo: (callback) => ipcRenderer.on('navigate-to', callback),
  removeNavigateToListener: () => ipcRenderer.removeAllListeners('navigate-to'),
  
  // Data export
  onExportData: (callback) => ipcRenderer.on('export-data', callback),
  removeExportDataListener: () => ipcRenderer.removeAllListeners('export-data'),
  
  // Window controls
  minimize: () => ipcRenderer.invoke('minimize-window'),
  maximize: () => ipcRenderer.invoke('maximize-window'),
  close: () => ipcRenderer.invoke('close-window'),
  
  // Health reminders
  scheduleReminder: (reminder) => ipcRenderer.invoke('schedule-reminder', reminder),
  cancelReminder: (id) => ipcRenderer.invoke('cancel-reminder', id),
  
  // File operations
  selectFile: (options) => ipcRenderer.invoke('select-file', options),
  saveFile: (options) => ipcRenderer.invoke('save-file', options),
  
  // System integration
  setLoginItemSettings: (settings) => ipcRenderer.invoke('set-login-item-settings', settings),
  getLoginItemSettings: () => ipcRenderer.invoke('get-login-item-settings'),
  
  // Clipboard
  writeToClipboard: (text) => ipcRenderer.invoke('write-to-clipboard', text),
  readFromClipboard: () => ipcRenderer.invoke('read-from-clipboard'),
});

// Desktop-specific features
contextBridge.exposeInMainWorld('desktopFeatures', {
  // Check if running in desktop
  isDesktop: true,
  
  // Platform-specific features
  platform: process.platform,
  
  // Desktop notifications
  supportsNotifications: true,
  
  // File system access
  supportsFileSystem: true,
  
  // System tray
  supportsTray: true,
  
  // Auto-updater
  supportsAutoUpdate: true,
  
  // Keyboard shortcuts
  supportsGlobalShortcuts: true,
});

// Health data sync
contextBridge.exposeInMainWorld('healthSync', {
  // Sync data with cloud
  syncData: (data) => ipcRenderer.invoke('sync-health-data', data),
  
  // Backup data locally
  backupData: (data) => ipcRenderer.invoke('backup-health-data', data),
  
  // Restore data from backup
  restoreData: () => ipcRenderer.invoke('restore-health-data'),
  
  // Export health report
  exportReport: (format, data) => ipcRenderer.invoke('export-health-report', format, data),
});

// Security features
contextBridge.exposeInMainWorld('security', {
  // Encrypt sensitive data
  encrypt: (data) => ipcRenderer.invoke('encrypt-data', data),
  
  // Decrypt sensitive data
  decrypt: (encryptedData) => ipcRenderer.invoke('decrypt-data', encryptedData),
  
  // Secure storage for sensitive information
  secureStore: {
    set: (key, value) => ipcRenderer.invoke('secure-store-set', key, value),
    get: (key) => ipcRenderer.invoke('secure-store-get', key),
    delete: (key) => ipcRenderer.invoke('secure-store-delete', key),
  },
});

// Development helpers
if (process.env.NODE_ENV === 'development') {
  contextBridge.exposeInMainWorld('devTools', {
    openDevTools: () => ipcRenderer.invoke('open-dev-tools'),
    reload: () => ipcRenderer.invoke('reload-window'),
    toggleDevTools: () => ipcRenderer.invoke('toggle-dev-tools'),
  });
}