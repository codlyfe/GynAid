import React from 'react';
import { Image, ImageProps, StyleSheet } from 'react-native';

// Healthcare-specific image optimization configuration
interface OptimizedImageProps extends ImageProps {
  priority?: 'normal' | 'low' | 'high';
  cacheStrategy?: 'cacheFirst' | 'webFirst' | 'cacheOnly';
  isCritical?: boolean; // For critical healthcare images (profile photos, important UI elements)
}

// Note: To enable FastImage optimization, install: npm install react-native-fast-image
// This component falls back to standard Image for now, but can be upgraded when dependency is available
const OptimizedImage: React.FC<OptimizedImageProps> = ({
  source,
  style,
  priority = 'normal',
  cacheStrategy = 'cacheFirst',
  isCritical = false,
  ...props
}) => {
  // Healthcare app image optimization - Performance enhancement plan
  // Currently uses standard Image, but ready for FastImage upgrade
  
  const imageSource = Array.isArray(source) ? source[0] : source;
  
  return (
    <Image
      source={imageSource}
      style={[styles.optimizedImage, style]}
      resizeMode="contain"
      // Performance hints for the current Image component
      {...props}
    />
  );
};

// When react-native-fast-image is installed, replace above with:
// const FastImage = require('react-native-fast-image').default;
// And update the component to use FastImage with:
// - Preloading for critical healthcare images
// - Caching strategies for offline functionality
// - Priority loading for important UI elements

const styles = StyleSheet.create({
  optimizedImage: {
    // Optimized styling for healthcare app images
    // Pre-configured for future FastImage integration
  }
});

export default OptimizedImage;