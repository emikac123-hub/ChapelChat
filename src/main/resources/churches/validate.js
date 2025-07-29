// validate.js
const Ajv = require("ajv");
const fs = require("fs");

const ajv = new Ajv();
const schema = JSON.parse(fs.readFileSync("church-profile.schema.json", "utf8"));
const validate = ajv.compile(schema);

const filenames = ["grace-orthodox.json", "cornerstone-catholic.json", "hope-baptist.json"];

filenames.forEach(file => {
  const data = JSON.parse(fs.readFileSync(file, "utf8"));
  const valid = validate(data);
  if (valid) {
    console.log(`✅ ${file} is valid`);
  } else {
    console.error(`❌ ${file} is invalid:`, validate.errors);
  }
});
