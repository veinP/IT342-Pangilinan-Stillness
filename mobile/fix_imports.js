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

files.forEach(f => {
  let c = fs.readFileSync(f, 'utf8');
  c = c.replace(/edu\.cit\.pangilinan\.stillness\.api\.ApiClient/g, 'edu.cit.pangilinan.stillness.shared.api.ApiClient');
  c = c.replace(/edu\.cit\.pangilinan\.stillness\.auth\.SessionManager/g, 'edu.cit.pangilinan.stillness.shared.auth.SessionManager');
  c = c.replace(/edu\.cit\.pangilinan\.stillness\.api\.SessionApi/g, 'edu.cit.pangilinan.stillness.features.sessions.SessionApi');
  c = c.replace(/edu\.cit\.pangilinan\.stillness\.api\.BookingApi/g, 'edu.cit.pangilinan.stillness.features.bookings.BookingApi');
  c = c.replace(/edu\.cit\.pangilinan\.stillness\.api\.QuoteApi/g, 'edu.cit.pangilinan.stillness.features.dashboard.QuoteApi');
  
  // Replace old root references to features
  c = c.replace(/import edu\.cit\.pangilinan\.stillness\.SessionAdapter/g, 'import edu.cit.pangilinan.stillness.features.sessions.SessionAdapter');
  c = c.replace(/import edu\.cit\.pangilinan\.stillness\.BookingAdapter/g, 'import edu.cit.pangilinan.stillness.features.bookings.BookingAdapter');
  c = c.replace(/import edu\.cit\.pangilinan\.stillness\.SessionDetailActivity/g, 'import edu.cit.pangilinan.stillness.features.sessions.SessionDetailActivity');
  c = c.replace(/import edu\.cit\.pangilinan\.stillness\.MyBookingsActivity/g, 'import edu.cit.pangilinan.stillness.features.bookings.MyBookingsActivity');
  c = c.replace(/import edu\.cit\.pangilinan\.stillness\.SessionsActivity/g, 'import edu.cit.pangilinan.stillness.features.sessions.SessionsActivity');
  c = c.replace(/import edu\.cit\.pangilinan\.stillness\.LoginActivity/g, 'import edu.cit.pangilinan.stillness.features.auth.LoginActivity');
  c = c.replace(/import edu\.cit\.pangilinan\.stillness\.RegisterActivity/g, 'import edu.cit.pangilinan.stillness.features.auth.RegisterActivity');
  c = c.replace(/import edu\.cit\.pangilinan\.stillness\.DashboardActivity/g, 'import edu.cit.pangilinan.stillness.features.dashboard.DashboardActivity');
  
  // Add R import if missing
  if (!c.includes('import edu.cit.pangilinan.stillness.R') && c.includes('R.')) {
    c = c.replace(/^package\s+(.*)$/m, 'package $1\n\nimport edu.cit.pangilinan.stillness.R');
  }
  fs.writeFileSync(f, c);
});
