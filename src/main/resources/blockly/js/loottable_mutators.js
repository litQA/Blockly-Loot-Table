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
                    .setAlign(Blockly.Input.Align.RIGHT)
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
                    .setAlign(Blockly.Input.Align.RIGHT)
                    .appendField(new FieldDataListSelector('enchantments'), 'enchantment' + index);
            },
            false,
            ['enchantment'],
            true),
        undefined,
        ['loottable_item_enchant_list_mutator_input']);

    // Container rows need two value inputs per row, so they cannot use the
    // single-input repeating mixin.
    Blockly.Blocks['loottable_item_container_mutator_container'] = {
        init: function () {
            this.appendDummyInput().appendField(label('blockly.block.loottable_item_container.mutator.container', 'Items'));
            this.appendStatementInput('STACK');
            this.setColour(colour);
            this.contextMenu = false;
        }
    };
    Blockly.Blocks['loottable_item_container_mutator_input'] = {
        init: function () {
            this.appendDummyInput().appendField(label('blockly.block.loottable_item_container.mutator.input', 'Item row'));
            this.setPreviousStatement(true);
            this.setNextStatement(true);
            this.setColour(colour);
            this.contextMenu = false;
        }
    };

    function label(key, fallback) {
        return typeof javabridge !== 'undefined' ? javabridge.t(key) : fallback;
    }

    function rowCount(block) {
        var count = parseInt(block.inputCount_, 10);
        if (!count || count < 1) {
            count = 1;
        }
        return Math.min(count, 64);
    }

    function updateContainerShape(block) {
        var count = rowCount(block);
        for (var i = 0; block.getInput('item' + i); i++) {
            block.removeInput('item' + i);
        }
        for (var j = 0; block.getInput('count' + j); j++) {
            block.removeInput('count' + j);
        }
        for (var row = 0; row < count; row++) {
            block.appendValueInput('item' + row).setCheck('MCItem')
                .setAlign(Blockly.Input.Align.RIGHT)
                .appendField(label('blockly.block.loottable_item_container.mutator.item', 'Item'));
            block.appendValueInput('count' + row).setCheck(['Number', 'NumberProvider'])
                .setAlign(Blockly.Input.Align.RIGHT)
                .appendField(label('blockly.block.loottable_item_container.mutator.count', 'Count'));
        }
    }

    Blockly.Extensions.registerMutator('loottable_item_container_mutator', {
        mutationToDom: function () {
            var mutation = document.createElement('mutation');
            mutation.setAttribute('inputs', rowCount(this));
            return mutation;
        },
        domToMutation: function (xmlElement) {
            this.inputCount_ = parseInt(xmlElement.getAttribute('inputs'), 10) || 1;
            updateContainerShape(this);
        },
        saveExtraState: function () {
            return {'inputCount': rowCount(this)};
        },
        loadExtraState: function (state) {
            this.inputCount_ = Math.max(1, Math.min(64, parseInt(state.inputCount, 10) || 1));
            updateContainerShape(this);
        },
        decompose: function (workspace) {
            var container = workspace.newBlock('loottable_item_container_mutator_container');
            container.initSvg();
            var connection = container.getInput('STACK').connection;
            for (var i = 0; i < rowCount(this); i++) {
                var row = workspace.newBlock('loottable_item_container_mutator_input');
                row.initSvg();
                connection.connect(row.previousConnection);
                connection = row.nextConnection;
            }
            return container;
        },
        compose: function (containerBlock) {
            var itemConnections = [];
            var countConnections = [];
            var row = containerBlock.getInputTargetBlock('STACK');
            while (row && !row.isInsertionMarker()) {
                itemConnections.push(row.itemConnection_);
                countConnections.push(row.countConnection_);
                row = row.nextConnection && row.nextConnection.targetBlock();
            }
            for (var i = 0; i < rowCount(this); i++) {
                var itemInput = this.getInput('item' + i);
                var countInput = this.getInput('count' + i);
                if (itemInput && itemInput.connection.targetConnection
                    && itemConnections.indexOf(itemInput.connection.targetConnection) === -1) {
                    itemInput.connection.targetConnection.disconnect();
                }
                if (countInput && countInput.connection.targetConnection
                    && countConnections.indexOf(countInput.connection.targetConnection) === -1) {
                    countInput.connection.targetConnection.disconnect();
                }
            }
            this.inputCount_ = Math.max(1, itemConnections.length);
            updateContainerShape(this);
            for (var index = 0; index < this.inputCount_; index++) {
                Blockly.Mutator.reconnect(itemConnections[index], this, 'item' + index);
                Blockly.Mutator.reconnect(countConnections[index], this, 'count' + index);
            }
        },
        saveConnections: function (containerBlock) {
            var row = containerBlock.getInputTargetBlock('STACK');
            var index = 0;
            while (row) {
                if (!row.isInsertionMarker()) {
                    var itemInput = this.getInput('item' + index);
                    var countInput = this.getInput('count' + index);
                    row.itemConnection_ = itemInput && itemInput.connection.targetConnection;
                    row.countConnection_ = countInput && countInput.connection.targetConnection;
                    index++;
                }
                row = row.getNextBlock();
            }
        },
        updateShape_: function () {
            updateContainerShape(this);
        }
    }, undefined, ['loottable_item_container_mutator_input']);

    // A-type condition aggregate: all/any with arbitrarily many value inputs.
    registerSimpleMutatorContainer('loottable_condition_mutator_container',
        'blockly.block.loottable_condition_mutator.container', 210);
    registerSimpleMutatorInput('loottable_condition_mutator_input',
        'blockly.block.loottable_condition_mutator.input', 210, false);
    function registerConditionAggregate(extensionName) {
        Blockly.Extensions.registerMutator(extensionName,
            simpleRepeatingInputMixin(
                'loottable_condition_mutator_container',
                'loottable_condition_mutator_input',
                'condition',
                function (thisBlock, inputName, index) {
                    thisBlock.appendValueInput(inputName + index)
                        .setCheck('LootTableCondition')
                        .setAlign(Blockly.Input.Align.RIGHT)
                        .appendField(label('blockly.block.loottable_condition_mutator.input', 'Condition'));
                },
                true,
                [],
                true),
            undefined,
            ['loottable_condition_mutator_input']);
    }
    registerConditionAggregate('loottable_condition_all_mutator');
    registerConditionAggregate('loottable_condition_any_mutator');

    // Alternatives pools. Every branch has an A-type condition and an entry statement.
    registerSimpleMutatorContainer('loottable_pool_mutator_container',
        'blockly.block.loottable_pool_mutator.container', 120);
    registerSimpleMutatorInput('loottable_pool_mutator_input',
        'blockly.block.loottable_pool_mutator.input', 120, false);

    function poolHasElse(block) {
        return block.type === 'loottable_pool_if_else';
    }

    function updatePoolShape(block) {
        if (!block) {
            return;
        }
        for (var index = 0; index < 64; index++) {
            if (block.getInput('condition' + index)) {
                block.removeInput('condition' + index);
            }
            if (block.getInput('entries' + index)) {
                block.removeInput('entries' + index);
            }
        }
        if (block.getInput('entries_else')) {
            block.removeInput('entries_else');
        }
        var count = Math.max(1, Math.min(64, block.inputCount_ || 1));
        for (var i = 0; i < count; i++) {
            var branchLabel = i === 0
                ? label('blockly.block.loottable_pool_mutator.if', 'If condition')
                : label('blockly.block.loottable_pool_mutator.else_if', 'Else if condition');
            block.appendValueInput('condition' + i)
                .setCheck('LootTableCondition')
                .setAlign(Blockly.Input.Align.RIGHT)
                .appendField(branchLabel);
            block.appendStatementInput('entries' + i)
                .setCheck('LootTableEntry')
                .setAlign(Blockly.Input.Align.RIGHT)
                .appendField(label('blockly.block.loottable_pool_mutator.entries', 'Entries'));
        }
        if (poolHasElse(block)) {
            block.appendStatementInput('entries_else')
                .setCheck('LootTableEntry')
                .setAlign(Blockly.Input.Align.RIGHT)
                .appendField(label('blockly.block.loottable_pool_mutator.else', 'Else entries'));
        }
    }

    function registerPoolMutator(extensionName) {
        Blockly.Extensions.registerMutator(extensionName, {
            mutationToDom: function () {
                var mutation = document.createElement('mutation');
                mutation.setAttribute('inputs', Math.max(1, Math.min(64, this.inputCount_ || 1)));
                return mutation;
            },
            domToMutation: function (xmlElement) {
                this.inputCount_ = Math.max(1, Math.min(64,
                    parseInt(xmlElement.getAttribute('inputs'), 10) || 1));
                updatePoolShape(this);
            },
            saveExtraState: function () {
                return {'inputCount': Math.max(1, Math.min(64, this.inputCount_ || 1))};
            },
            loadExtraState: function (state) {
                this.inputCount_ = Math.max(1, Math.min(64,
                    parseInt(state.inputCount, 10) || 1));
                updatePoolShape(this);
            },
            decompose: function (workspace) {
                var container = workspace.newBlock('loottable_pool_mutator_container');
                container.initSvg();
                var connection = container.getInput('STACK').connection;
                for (var i = 0; i < Math.max(1, this.inputCount_ || 1); i++) {
                    var row = workspace.newBlock('loottable_pool_mutator_input');
                    row.initSvg();
                    connection.connect(row.previousConnection);
                    connection = row.nextConnection;
                }
                return container;
            },
            compose: function (containerBlock) {
                var conditionConnections = [];
                var entriesConnections = [];
                var row = containerBlock.getInputTargetBlock('STACK');
                while (row && !row.isInsertionMarker()) {
                    conditionConnections.push(row.conditionConnection_);
                    entriesConnections.push(row.entriesConnection_);
                    row = row.nextConnection && row.nextConnection.targetBlock();
                }
                for (var i = 0; i < 64; i++) {
                    var conditionInput = this.getInput('condition' + i);
                    var entriesInput = this.getInput('entries' + i);
                    if (conditionInput && conditionInput.connection.targetConnection
                        && conditionConnections.indexOf(conditionInput.connection.targetConnection) === -1) {
                        conditionInput.connection.targetConnection.disconnect();
                    }
                    if (entriesInput && entriesInput.connection.targetConnection
                        && entriesConnections.indexOf(entriesInput.connection.targetConnection) === -1) {
                        entriesInput.connection.targetConnection.disconnect();
                    }
                }
                this.inputCount_ = Math.max(1, conditionConnections.length);
                updatePoolShape(this);
                for (var index = 0; index < this.inputCount_; index++) {
                    Blockly.Mutator.reconnect(conditionConnections[index], this, 'condition' + index);
                    Blockly.Mutator.reconnect(entriesConnections[index], this, 'entries' + index);
                }
            },
            saveConnections: function (containerBlock) {
                var row = containerBlock.getInputTargetBlock('STACK');
                var index = 0;
                while (row) {
                    if (!row.isInsertionMarker()) {
                        var conditionInput = this.getInput('condition' + index);
                        var entriesInput = this.getInput('entries' + index);
                        row.conditionConnection_ = conditionInput && conditionInput.connection.targetConnection;
                        row.entriesConnection_ = entriesInput && entriesInput.connection.targetConnection;
                        index++;
                    }
                    row = row.getNextBlock();
                }
            },
            updateShape_: function () {
                updatePoolShape(this);
            }
        }, undefined, ['loottable_pool_mutator_input']);
    }
    registerPoolMutator('loottable_pool_if_mutator');
    registerPoolMutator('loottable_pool_if_else_if_mutator');
    registerPoolMutator('loottable_pool_if_else_mutator');
})();
