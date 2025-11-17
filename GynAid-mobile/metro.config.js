const { getDefaultConfig } = require('expo/metro-config');
const { withNativeWind } = require('nativewind/metro');

const config = getDefaultConfig(__dirname);

// Performance optimizations for healthcare app
config.resolver.platforms = ['ios', 'android', 'native', 'web'];

// Optimize bundle size by reducing resolver complexity
config.resolver.unstable_enableSymlink = false;
config.resolver.unstable_enablePackageExports = false;

// Configure transformer for better performance with enhanced minifier
config.transformer.minifierConfig = {
  keep_fnames: true,
  keep_classnames: true,
  mangle: {
    keep_fnames: true,
    keep_classnames: true,
  },
};

// Enhanced cache configuration
config.cacheStores = [
  require('metro-cache'),
  require('metro-cache-key'),
  require('metro-transformer')
];

// Enable HMR for faster development
if (process.env.NODE_ENV === 'development') {
  config.resolver.platforms.push('web');
}

// Performance optimization configurations
config.maxWorkers = Math.floor(require('os').cpus().length / 2);

// Bundle analysis and optimization settings
config.bundler = {
  // Enable experimental bundle splitting for better performance
  unstable_experimentalSplitBundleChunks: true,
  // Optimize dependency resolution
  resolver: {
    unstable_enableSymlink: false,
    resolverMainFields: ['react-native', 'browser', 'main'],
  },
};

// Source map configuration for debugging
if (process.env.NODE_ENV === 'development') {
  config.transformer.sourceMapOptions = {
    includeSources: true
  };
}

// Custom resolver aliases for cleaner imports
config.resolver.alias = {
  '@': './src',
  '@assets': './assets',
  '@components': './src/components',
  '@screens': './src/screens',
  '@services': './src/services',
  '@utils': './src/utils',
  '@hooks': './src/hooks',
  '@types': './src/types'
};

// Watch folders for better performance
config.watchFolders = [
  './src',
  './assets',
  './app.json'
];

module.exports = withNativeWind(config, { input: './src/index.css' });