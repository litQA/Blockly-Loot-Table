(function () {
    if (typeof registerSimpleMutatorContainer !== 'function'
        || typeof registerSimpleMutatorInput !== 'function'
        || typeof simpleRepeatingInputMixin !== 'function') {
        return;
    }

    var colour = 350;

    // Enchantment rows with an A-type Number/NumberProvider level input.
    registerSimpleMutatorContainer('loottable_item_enchantments_mutator_container',
        'blockly.block.loottable_item_with_enchantment.mutator.container', colour);
    registerSimpleMutatorInput('loottable_item_enchantments_mutator_input',
        'blockly.block.loottable_item_with_enchantment.mutator.input', colour, true);
    Blockly.Extensions.registerMutator('loottable_item_with_enchantment_mutator',
        simpleRepeatingInputMixin(
            'loottable_item_enchantments_mutator_container',
            'loottable_item_enchantments_mutator_input',
            'level',
            function (thisBlock, inputName, index) {
                thisBlock.appendValueInput(inputName + index)
                    .setCheck(['Number', 'NumberProvider'])
                    .appendField(new FieldDataListSelector('enchantments'), 'enchantment' + index);
            },
            true,
            ['enchantment'],
            true),
        undefined,
        ['loottable_item_enchantments_mutator_input']);

    // Enchantment-id lists without a level input.
    registerSimpleMutatorContainer('loottable_item_enchant_list_mutator_container',
        'blockly.block.loottable_item_enchant_list.mutator.container', colour);
    registerSimpleMutatorInput('loottable_item_enchant_list_mutator_input',
        'blockly.block.loottable_item_enchant_list.mutator.input', colour, true);
    Blockly.Extensions.registerMutator('loottable_item_enchant_list_mutator',
        simpleRepeatingInputMixin(
            'loottable_item_enchant_list_mutator_container',
            'loottable_item_enchant_list_mutator_input',
            'row',
            function (thisBlock, inputName, index) {
                thisBlock.appendDummyInput(inputName + index)
                    .appendField(new FieldDataListSelector('enchantments'), 'enchantment' + index);
            },
            false,
            ['enchantment'],
            true),
        undefined,
        ['loottable_item_enchant_list_mutator_input']);
})();
