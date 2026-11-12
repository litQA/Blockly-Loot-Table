(function () {
    if (typeof Blockly === 'undefined') {
        return;
    }

    function patchStartBlock() {
        var definition = Blockly.Blocks && Blockly.Blocks['loottable_start'];
        if (!definition || typeof definition.init !== 'function' || definition.__lootTableStartPatched) {
            return;
        }

        var originalInit = definition.init;
        definition.init = function () {
            originalInit.call(this);
            this.setDeletable(false);
        };
        definition.__lootTableStartPatched = true;
    }

    var originalDefineBlocks = Blockly.defineBlocksWithJsonArray;
    if (typeof originalDefineBlocks === 'function') {
        Blockly.defineBlocksWithJsonArray = function () {
            var result = originalDefineBlocks.apply(this, arguments);
            patchStartBlock();
            return result;
        };
    }

    patchStartBlock();
})();
