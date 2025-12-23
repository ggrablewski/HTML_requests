import https from 'https';
// const https = require('https');

const users = [];

// fetch('https://jsonplaceholder.typicode.com/users')
// .then(response => response.json())
// .then(data => {
//     data.forEach(user => users.push(user.name));
//     console.log('User Names:', users);
// });

https.get('https://jsonplaceholder.typicode.com/users', (resp) => {
  let data = '';
  resp.on('data', (chunk) => {
    data += chunk;
  });
  resp.on('end', () => {
    const json = JSON.parse(data);
    json.forEach(user => users.push(user.name));
    console.log('User Names:', users);
  });
});
