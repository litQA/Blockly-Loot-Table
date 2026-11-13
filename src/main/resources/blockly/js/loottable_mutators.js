(function () {
    if (typeof registerSimpleMutatorContainer !== 'function'
        || typeof registerSimpleMutatorInput !== 'function'
        || typeof simpleRepeatingInputMixin !== 'function') {
        return;
    }

    var colour = 350;
    registerSimpleMutatorContainer('loottable_item_enchantments_mutator_container',
        'blockly.block.loottable_item_has_enchantments.mutator.container', colour);
    registerSimpleMutatorInput('loottable_item_enchantments_mutator_input',
        'blockly.block.loottable_item_has_enchantments.mutator.input', colour, true);

    Blockly.Extensions.registerMutator('loottable_item_has_enchantments_mutator',
        simpleRepeatingInputMixin(
            'loottable_item_enchantments_mutator_container',
            'loottable_item_enchantments_mutator_input',
            'row',
            function (thisBlock, inputName, index) {
                thisBlock.appendDummyInput(inputName + index)
                    .appendField(new FieldDataListSelector('enchantments'), 'enchantment' + index)
                    .appendField(javabridge.t('blockly.block.loottable_item_has_enchantments.level'))
                    .appendField(new Blockly.FieldNumber(1, 1, 255, 1), 'level' + index);
            },
            false,
            ['enchantment', 'level'],
            true),
        undefined,
        ['loottable_item_enchantments_mutator_input']);
})();
