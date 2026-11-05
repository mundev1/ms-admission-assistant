import re
import subprocess
import sys

with open("/Users/mundevanujtomar/.gemini/antigravity/scratch/ms-admissions-engine/frontend/dashboard.html", "r") as f:
    html = f.read()

m = re.search(r"<script type=\"module\">([\s\S]*?)</script>", html)
if not m:
    print("Script not found!")
    sys.exit(1)

code = m.group(1)
# Clean imports
cleaned = re.sub(r"import\s+[\s\S]*?from\s+[\x27\x22].*?[\x27\x22];?", "// import removed", code)

# Mock environment
mock_header = """
(function() {
  function el() {
    return {
      textContent: "",
      className: "",
      innerHTML: "",
      classList: { add: function(){}, remove: function(){}, contains: function(){ return false; }, toggle: function(){} },
      setAttribute: function(){},
      getAttribute: function(){ return ""; },
      removeAttribute: function(){},
      addEventListener: function(){},
      dataset: {},
      style: {},
      querySelector: function() { return el(); },
      querySelectorAll: function() { return [el()]; },
      closest: function() { return el(); },
      appendChild: function() {},
      remove: function() {},
      focus: function() {}
    };
  }
  var _q = function() { return el(); };
  var _qq = function() { return [el()]; };
  var setTimeout = function(cb, ms) { if (cb) cb(); return 1; };
  var clearTimeout = function() {};
  var setInterval = function(cb, ms) { return 1; };
  var clearInterval = function() {};
  var requestAnimationFrame = function(cb) { if (cb) cb(); return 1; };
  var cancelAnimationFrame = function() {};
  var window = {
    addEventListener: function(){},
    matchMedia: function(){ return { matches: false, addEventListener: function(){} }; },
    sessionStorage: { getItem: function(){ return null; }, setItem: function(){} },
    location: { origin: "https://example.com" },
    setTimeout: setTimeout,
    clearTimeout: clearTimeout,
    requestAnimationFrame: requestAnimationFrame
  };
  var sessionStorage = window.sessionStorage;
  var document = {
    addEventListener: function(){},
    querySelector: _q,
    querySelectorAll: _qq,
    getElementById: _q,
    createElement: function() { return el(); },
    body: el()
  };
"""

# Replace $ and $$ definitions and calls cleanly
script_body = cleaned.replace("function $(sel)", "function _q(sel)")
script_body = script_body.replace("function $$(sel)", "function _qq(sel)")
script_body = re.sub(r"\$\$(?=\()", "_qq", script_body)
script_body = re.sub(r"\$(?=\()", "_q", script_body)

mock_footer = """
  return "VALID_SYNTAX";
})();
"""

full_test_js = mock_header + script_body + mock_footer

with open("/tmp/validate_dashboard.js", "w") as out:
    out.write(full_test_js)

res = subprocess.run([
    "osascript", "-l", "JavaScript", "-e",
    'run = function() { var code = $.NSString.stringWithContentsOfFileEncodingError("/tmp/validate_dashboard.js", $.NSUTF8StringEncoding, null).js; return eval(code); }'
], capture_output=True, text=True)

print("Returncode:", res.returncode)
print("STDOUT:", res.stdout.strip())
print("STDERR:", res.stderr.strip())
if res.stdout.strip() == "VALID_SYNTAX":
    print(">>> VERIFICATION PASSED: dashboard.html JavaScript is 100% syntactically valid! <<<")
else:
    print(">>> VERIFICATION FAILED <<<")
