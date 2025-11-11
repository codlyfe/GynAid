#!/usr/bin/env python3
import os
import re

def fix_package_declarations(root_dir):
    """Fix all package declarations from com.GynaId.backend to com.gynaid.backend"""
    
    for dirpath, dirnames, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.endswith('.java'):
                filepath = os.path.join(dirpath, filename)
                
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        content = f.read()
                    
                    # Fix package declarations
                    original_content = content
                    content = re.sub(r'package com\.GynaId\.backend;', 'package com.gynaid.backend;', content)
                    
                    # Only write if changes were made
                    if content != original_content:
                        with open(filepath, 'w', encoding='utf-8') as f:
                            f.write(content)
                        print(f"Fixed: {filepath}")
                        
                except Exception as e:
                    print(f"Error processing {filepath}: {e}")

if __name__ == "__main__":
    fix_package_declarations("GynAid-backend/src/main/java")