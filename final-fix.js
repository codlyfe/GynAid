const fs = require('fs');
const path = require('path');

function fixJavaPackagesRecursive(startPath) {
    const files = fs.readdirSync(startPath);
    
    for (const file of files) {
        const filePath = path.join(startPath, file);
        const stat = fs.statSync(filePath);
        
        if (stat.isDirectory()) {
            fixJavaPackagesRecursive(filePath);
        } else if (file.endsWith('.java')) {
            let content = fs.readFileSync(filePath, 'utf8');
            const original = content;
            
            // Fix package declarations
            content = content.replace(/package com\.GynaId\.backend;/g, 'package com.gynaid.backend;');
            
            // Fix import statements
            content = content.replace(/import com\.GynaId\.backend\./g, 'import com.gynaid.backend.');
            
            if (content !== original) {
                fs.writeFileSync(filePath, content);
                console.log('Fixed:', filePath);
            }
        }
    }
}

console.log('Starting final comprehensive fix...');
fixJavaPackagesRecursive('GynAid-backend/src/main/java/com/gynaid/backend');
console.log('Final fix completed!');