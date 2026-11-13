(function () {
    if (typeof Blockly === 'undefined' || typeof Blockly.FieldTextInput !== 'function') {
        return;
    }

    class FieldLootTableFloat extends Blockly.FieldTextInput {
        constructor(text) {
            super(text == null ? '1.0' : String(text));
        }

        doClassValidation_(newValue) {
            if (typeof newValue === 'number') {
                newValue = String(newValue);
            }
            if (typeof newValue !== 'string') {
                return null;
            }

            var text = newValue.trim();
            if (!/^(?:\d+(?:\.\d*)?|\.\d+)$/.test(text)) {
                return null;
            }

            var number = Number(text);
            if (!isFinite(number) || number < 0 || number > 64000) {
                return null;
            }

            // Keep an explicit decimal point so the float block is visually distinct from an integer.
            if (number === Math.floor(number)) {
                return number.toFixed(1);
            }
            return String(number);
        }

        static fromJson(options) {
            return new FieldLootTableFloat(options['text'] ?? options['value'] ?? '1.0');
        }
    }

    Blockly.fieldRegistry.register('field_loottable_float', FieldLootTableFloat);
})();
