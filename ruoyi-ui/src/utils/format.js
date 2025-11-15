export function formatDate(date, fmt = 'yyyy-MM-dd hh:mm:ss') {
  if (!date) {
    return '';
  }

  let dateObj;
  // If it's already a Date object, use it directly.
  if (date instanceof Date) {
    dateObj = date;
  }
  // If it's a string, parse it.
  else if (typeof date === 'string') {
    // Create a new Date object directly from the string.
    // Modern browsers are good at parsing ISO 8601 formats (like "2025-11-16T04:17:09" or "2025-11-16 04:17:09").
    // The 'T' is a standard separator. If it's a space, replacing it with 'T' can improve consistency.
    dateObj = new Date(date.includes('T') ? date : date.replace(' ', 'T'));
  }
  // If it's a number (timestamp), create a Date from it.
  else if (typeof date === 'number') {
    dateObj = new Date(date);
  }
  // If it's none of the above, we can't format it.
  else {
    return '';
  }

  // After attempting to create a date, check if it's valid.
  if (isNaN(dateObj.getTime())) {
    // If parsing failed, try replacing hyphens for broader compatibility, which helps with "yyyy-MM-dd" formats.
    if (typeof date === 'string') {
        dateObj = new Date(date.replace(/-/g, "/"));
        if (isNaN(dateObj.getTime())) {
            return ''; // Still invalid, return empty.
        }
    } else {
        return ''; // Not a string and invalid, return empty.
    }
  }

  const o = {
    'M+': dateObj.getMonth() + 1,
    'd+': dateObj.getDate(),
    'h+': dateObj.getHours(),
    'm+': dateObj.getMinutes(),
    's+': dateObj.getSeconds(),
    'q+': Math.floor((dateObj.getMonth() + 3) / 3),
    'S': dateObj.getMilliseconds()
  };

  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (dateObj.getFullYear() + '').substr(4 - RegExp.$1.length));
  }

  for (let k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      const str = o[k] + '';
      fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? str : ('00' + str).substr(str.length));
    }
  }
  return fmt;
}

