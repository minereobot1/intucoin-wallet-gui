import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, Input, Output, forwardRef, EventEmitter } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

declare const monaco: any;
declare const require: any;

@Component({
    selector: 'monaco-editor',
    templateUrl: './monaco-editor.component.html',
    styleUrls: ['./monaco-editor.component.css'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => MonacoEditorComponent),
            multi: true
        }
    ],
})

export class MonacoEditorComponent implements OnInit, AfterViewInit {

    static loaded = false;

    @ViewChild('editor') editorContent: ElementRef;
    @Input() language: string;
    @Input() language_defaults: any = null;
    @Input() options: any = {};

    @Input() set value (v: string) {
        if (v !== this._value) {
            this._value = v;
            this.onChange(v);
        }
    }

    @Output() change = new EventEmitter();
    @Output() instance = null;

    private _editor: any;
    private _value = '';
    private _javascriptExtraLibs: any = null;
    private _typescriptExtraLibs: any = null;

    constructor () {
    }

    get value (): string {
        return this._value;
    }

;

    ngOnInit () {
    }

    ngAfterViewInit () {
        console.log("After view init");
        var onGotAmdLoader = () => {
            // Load monaco
            console.log("Loading Monaco");
            if (!MonacoEditorComponent.loaded) {
                (<any>window).require.config({paths: {'vs': 'assets/monaco/vs'}});
                (<any>window).require(['vs/editor/editor.main'], () => {
                    console.log("Monaco was loaded")
                    this.initMonaco();
                    MonacoEditorComponent.loaded = true;
                });
            } else {
                this.initMonaco();
            }
        };

        // Load AMD loader if necessary
        if (!(<any>window).require) {
            console.log("Loading AMD Loader");
            var loaderScript = document.createElement('script');
            loaderScript.type = 'text/javascript';
            loaderScript.src = 'assets/monaco/vs/loader.js';
            loaderScript.addEventListener('load', onGotAmdLoader);
            document.body.appendChild(loaderScript);
        } else {
            onGotAmdLoader();
        }
    }

    /**
     * Upon destruction of the component we make sure to dispose both the editor and the extra libs that we might've loaded
     */
    ngOnDestroy () {
        if (this._editor !== undefined) {
            this._editor.dispose();
        }
        if (this._javascriptExtraLibs !== undefined && this._javascriptExtraLibs !== null) {
            this._javascriptExtraLibs.dispose();
        }

        if (this._typescriptExtraLibs !== undefined && this._typescriptExtraLibs !== null) {
            this._typescriptExtraLibs.dispose();
        }
    }

    // Will be called once monaco library is available
    initMonaco () {
        console.log("Init Monaco-Editor");
        var myDiv: HTMLDivElement = this.editorContent.nativeElement;
        let options = this.options;
        options.value = this._value;
        options.language = this.language;

        this._editor = monaco.editor.create(myDiv, options);

        // Set language defaults
        // We already set the language on the component so we act accordingly
        if (this.language_defaults !== null) {
            switch (this.language) {
                case 'javascript':
                    monaco.languages.typescript.javascriptDefaults.setCompilerOptions(
                        this.language_defaults.compilerOptions
                    );
                    for (var extraLib in this.language_defaults.extraLibs) {
                        this._javascriptExtraLibs = monaco.languages.typescript.javascriptDefaults.addExtraLib(
                            this.language_defaults.extraLibs[extraLib].definitions,
                            this.language_defaults.extraLibs[extraLib].definitions_name
                        );
                    }
                    break;
                case 'typescript':
                    monaco.languages.typescript.typescriptDefaults.setCompilerOptions(
                        this.language_defaults.compilerOptions
                    );
                    for (var extraLib in this.language_defaults.extraLibs) {
                        this._typescriptExtraLibs = monaco.languages.typescript.typescriptDefaults.addExtraLib(
                            this.language_defaults.extraLibs[extraLib].definitions,
                            this.language_defaults.extraLibs[extraLib].definitions_name
                        );
                    }
                    break;
            }
        }

        this._editor.getModel().onDidChangeContent((e)=> {
            this.updateValue(this._editor.getModel().getValue());
        });
    }

    /**
     * UpdateValue
     *
     * @param value
     */
    updateValue (value: string) {
        this.value = value;
        this.onChange(value);
        this.onTouched();
        this.change.emit(value);
    }

    /**
     * WriteValue
     * Implements ControlValueAccessor
     *
     * @param value
     */
    writeValue (value: string) {
        this._value = value || '';
        console.log("Writing value", this._value);
        if (this.instance) {
            this.instance.setValue(this._value);
        }
        // If an instance of Monaco editor is running, update its contents
        if (this._editor) {
            this._editor.getModel().setValue(this._value);
        }
    }

    onChange (_) {
    }

    onTouched () {
    }

    registerOnChange (fn) {
        this.onChange = fn;
    }

    registerOnTouched (fn) {
        this.onTouched = fn;
    }

}