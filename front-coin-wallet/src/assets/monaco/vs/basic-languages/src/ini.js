/*!-----------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * monaco-languages version: 0.7.0(18916e97a4ff0f1b195d68d01d632631cc84d50e)
 * Released under the MIT license
 * https://github.com/Microsoft/monaco-languages/blob/master/LICENSE.md
 *-----------------------------------------------------------------------------*/
define("vs/basic-languages/src/ini", ["require", "exports"], function (n, e) {
    e.conf = {
        comments: {lineComment: "#"},
        brackets: [["{", "}"], ["[", "]"], ["(", ")"], ["<", ">"]],
        autoClosingPairs: [{open: '"', close: '"', notIn: ["string", "comment"]}, {
            open: "'",
            close: "'",
            notIn: ["string", "comment"]
        }, {open: "{", close: "}", notIn: ["string", "comment"]}, {
            open: "[",
            close: "]",
            notIn: ["string", "comment"]
        }, {open: "(", close: ")", notIn: ["string", "comment"]}, {open: "<", close: ">", notIn: ["string", "comment"]}]
    }, e.language = {
        defaultToken: "",
        tokenPostfix: ".ini",
        escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
        tokenizer: {
            root: [[/^\[[^\]]*\]/, "metatag"], [/(^\w+)(\s*)(\=)/, ["key", "", "delimiter"]], {include: "@whitespace"}, [/\d+/, "number"], [/"([^"\\]|\\.)*$/, "string.invalid"], [/'([^'\\]|\\.)*$/, "string.invalid"], [/"/, "string", '@string."'], [/'/, "string", "@string.'"]],
            whitespace: [[/[ \t\r\n]+/, ""], [/^\s*[#;].*$/, "comment"]],
            string: [[/[^\\"']+/, "string"], [/@escapes/, "string.escape"], [/\\./, "string.escape.invalid"], [/["']/, {
                cases: {
                    "$#==$S2": {
                        token: "string",
                        next: "@pop"
                    }, "@default": "string"
                }
            }]]
        }
    }
});