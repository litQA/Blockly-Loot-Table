<#assign generationErrors = data.getGenerationErrors()>
<#if generationErrors?has_content>
<#stop "Blockly Loot Table generation blocked: " + generationErrors?join(" | ")>
</#if>
${data.getGeneratedJson()}
