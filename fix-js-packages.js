#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

function fixJavaPackages(dir) {
    const files = fs.readdirSync(dir);
    
    for (const file of files) {
        const filePath = path.join(dir, file);
        const stat = fs.statSync(filePath);
        
        if (stat.isDirectory()) {
            fixJavaPackages(filePath);
        } else if (file.endsWith('.java')) {
            let content = fs.readFileSync(filePath, 'utf8');
            const original = content;
            content = content.replace(/package com\.GynaId\.backend;/g, 'package com.gynaid.backend;');
            
            if (content !== original) {
                fs.writeFileSync(filePath, content);
                console.log('Fixed:', filePath);
            }
        }
    }
}

fixJavaPackages('GynAid-backend/src/main/java');