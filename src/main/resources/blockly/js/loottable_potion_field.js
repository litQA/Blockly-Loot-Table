(function () {
    if (typeof Blockly === 'undefined' || typeof Blockly.FieldDropdown !== 'function') {
        return;
    }

    function label(key, fallback) {
        try {
            var value = javabridge.t(key);
            return value || fallback;
        } catch (ignored) {
            return fallback;
        }
    }

    class FieldLootTablePotionType extends Blockly.FieldDropdown {
        constructor(value) {
            super([
                [label('blockly.block.loottable_item_potion.type.potion', 'Potion'), 'potion'],
                [label('blockly.block.loottable_item_potion.type.splash_potion', 'Splash potion'), 'splash_potion'],
                [label('blockly.block.loottable_item_potion.type.lingering_potion', 'Lingering potion'), 'lingering_potion'],
                [label('blockly.block.loottable_item_potion.type.tipped_arrow', 'Tipped arrow'), 'tipped_arrow']
            ]);
            this.setValue(value || 'potion');
        }

        static fromJson(options) {
            return new FieldLootTablePotionType(options.value);
        }
    }

    Blockly.fieldRegistry.register('field_loottable_potion_type', FieldLootTablePotionType);
})();
