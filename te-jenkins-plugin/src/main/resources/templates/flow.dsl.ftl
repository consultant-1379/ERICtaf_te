<#if ( ! stopOnFail)>ignore(FAILURE){</#if>
build('${tafJobName}',
<#list params as param >
${param[0]}:'${param[1]}'<#if param_has_next>,</#if>
</#list>
)
<#if ( ! stopOnFail)>}</#if>
