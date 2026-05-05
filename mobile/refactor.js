const fs = require('fs');
const path = require('path');

const basePath = 'app/src/main/java/edu/cit/pangilinan/stillness';

const walkSync = (dir, filelist = []) => {
  fs.readdirSync(dir).forEach(file => {
    const dirFile = path.join(dir, file);
    if (fs.statSync(dirFile).isDirectory()) {
      filelist = walkSync(dirFile, filelist);
    } else if (file.endsWith('.kt')) {
      filelist.push(dirFile);
    }
  });
  return filelist;
};

const files = walkSync(basePath);
const classes = {};

// Pass 1: Set correct packages and map classes
files.forEach(file => {
  const relPath = path.relative(basePath, file).replace(/\\/g, '/');
  const dirName = path.dirname(relPath);
  let pkg = 'edu.cit.pangilinan.stillness';
  if (dirName !== '.') {
    pkg += '.' + dirName.replace(/\//g, '.');
  }
  
  let content = fs.readFileSync(file, 'utf8');
  content = content.replace(/^package\s+.*$/m, `package ${pkg}`);
  fs.writeFileSync(file, content);
  
  const className = path.basename(file, '.kt');
  // Some files might have multiple classes or functions, but we assume the main one matches filename
  classes[className] = pkg + '.' + className;
});

// Pass 2: Add imports
files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  const importsToAdd = new Set();
  
  // also fix AndroidManifest manually later
  
  Object.keys(classes).forEach(cls => {
    // Only import if it's actually used as a word
    const regex = new RegExp(`\\b${cls}\\b`);
    if (regex.test(content)) {
       const classPkg = classes[cls].substring(0, classes[cls].lastIndexOf('.'));
       const currentPkgMatch = content.match(/^package\s+(.*)$/m);
       const currentPkg = currentPkgMatch ? currentPkgMatch[1] : '';
       
       if (currentPkg !== classPkg) {
         // check if already imported
         if (!content.includes(`import ${classes[cls]}`)) {
           importsToAdd.add(`import ${classes[cls]}`);
         }
       }
    }
  });
  
  if (importsToAdd.size > 0) {
    const importStr = Array.from(importsToAdd).join('\n') + '\n';
    content = content.replace(/^(package\s+.*)$/m, `$1\n\n${importStr}`);
    fs.writeFileSync(file, content);
  }
});
