const fs = require('fs');
const path = require('path');

function fixAllFiles() {
    const rootDir = 'GynAid-backend/src/main/java';
    
    function processDirectory(dir) {
        const files = fs.readdirSync(dir);
        
        for (const file of files) {
            const filePath = path.join(dir, file);
            const stat = fs.statSync(filePath);
            
            if (stat.isDirectory()) {
                processDirectory(filePath);
            } else if (file.endsWith('.java')) {
                let content = fs.readFileSync(filePath, 'utf8');
                const original = content;
                
                // Fix package declarations - comprehensive regex
                content = content.replace(/package com\.GynaId\.backend[.\w]*;/g, (match) => {
                    return match.replace(/com\.GynaId\.backend/, 'com.gynaid.backend');
                });
                
                // Fix import statements - comprehensive regex
                content = content.replace(/import com\.GynaId\.backend[.\w]*\./g, (match) => {
                    return match.replace(/com\.GynaId\.backend\./, 'com.gynaid.backend.');
                });
                
                if (content !== original) {
                    fs.writeFileSync(filePath, content);
                    console.log('Fixed:', filePath);
                }
            }
        }
    }
    
    processDirectory(rootDir);
}

console.log('Starting ultimate comprehensive fix...');
fixAllFiles();
console.log('Ultimate fix completed!');