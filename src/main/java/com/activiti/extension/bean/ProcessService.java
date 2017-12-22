package com.activiti.extension.bean;

import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.runtime.ActivitiService;
import com.activiti.util.TenantHelper;

@Service
public class ProcessService {

	private static Logger LOG = LoggerFactory.getLogger(ProcessService.class);

	@Autowired
	private ActivitiService activitiService;

	@Autowired
	protected RepositoryService repositoryService;

	@Autowired
	protected RuntimeService runtimeService;

	public ProcessInstance startProcess(String processDefinitionKey, Map<String, Object> variables,
			String processInstanceName, String businessKey) {

		ProcessDefinition processDefinition = getProcessdefinition(processDefinitionKey);

		LOG.debug("Starte Prozess {} mit Business Key '{}'", processDefinition.getId(), businessKey);

		ProcessInstance processInstance = activitiService.startProcessInstance(processDefinition.getId(), variables,
				processInstanceName, businessKey);

		return processInstance;

	}

	/**
	 * 
	 * Sucht eine Prozessdefinition fuer {@code processDefinitionKey} und
	 * startet eine neue Prozessinstanz anhand einer {@code message}.
	 * 
	 * @param processDefinitionKey
	 *            Processdefinition Key
	 * @param message
	 *            Die Start Message
	 * @param variables
	 *            Variablen die an die Prozessinstanz uebergeben werden sollen
	 * @param processInstanceName
	 *            Name der Prozessinstanz
	 * @param businessKey
	 *            Business Key der Prozessinstanz
	 * @return Eine Referenz auf die gestartete {@link ProcessInstance}
	 * @return Eine Referenz auf die gestartete {@link ProcessInstance}
	 * @throws InternalServerErrorException
	 *             Falls keine Prozessdefinition zum Key
	 *             {@code processDefinitionKey} gefunden werden konnte
	 */
	public ProcessInstance startProcessWithMessage(String processDefinitionKey, String messageName,
			Map<String, Object> variables, String processInstanceName, String businessKey) {

		ProcessDefinition processDefinition = getProcessdefinition(processDefinitionKey);

		LOG.debug("Starte Prozess {} mit Business Key '{}' und Message '{}'", processDefinition.getId(), businessKey,
				messageName);

		ProcessInstance processInstance = this.runtimeService.startProcessInstanceByMessageAndTenantId(messageName,
				businessKey, variables, TenantHelper.getTenantIdForCurrentUser());

		this.runtimeService.setProcessInstanceName(processInstance.getProcessInstanceId(), processInstanceName);

		return processInstance;

	}

	private ProcessDefinition getProcessdefinition(String processDefinitionKey) {

		LOG.debug("Suche nach Prozessdefinition zum Key '{}'", processDefinitionKey);

		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
		if (processDefinition == null) {
			throw new InternalServerErrorException(
					"Es wurde keine Prozessdefinition fuer den Key '" + processDefinitionKey + "' gefunden");
		}

		return processDefinition;

	}

	public Execution findProcessByBusinessKey(String businessKey) {
		LOG.debug("Finde Prozess fuer den Business Key '{}'", businessKey);
		return runtimeService.createExecutionQuery().processInstanceBusinessKey(businessKey).singleResult();
	}

	public Execution findProcessByExecutionId(String executionId) {
		LOG.debug("Finde Prozess fuer den Execution ID '{}'", executionId);
		return runtimeService.createExecutionQuery().executionId(executionId).singleResult();
	}

	public Execution getMessageEventExecution(String messageEvent, String businessKey) {
		return this.runtimeService.createExecutionQuery().processInstanceBusinessKey(businessKey, true)
				.messageEventSubscriptionName(messageEvent).singleResult();
	}

	public void messageEventReceived(String messageEvent, Execution execution) {
		this.runtimeService.messageEventReceived(messageEvent, execution.getId());
	}

	public void messageEventReceived(String messageEvent, Execution execution, Map<String, Object> processVariables) {
		this.runtimeService.messageEventReceived(messageEvent, execution.getId(), processVariables);
	}

	public void messageEventReceived(String messageEvent, String businessKey) {
		Execution execution = this.getMessageEventExecution(messageEvent, businessKey);
		if (execution == null) {
			throw new NotFoundException("Es wurde keine Execution fuer das Message Event '" + messageEvent
					+ "' und fuer den Business Key '" + businessKey + "' gefunden");
		}
		this.messageEventReceived(messageEvent, execution);
	}

	public void setProcessVariable(String businessKey, String variableName, String value, String messageEvent) {
		Execution e = runtimeService.createExecutionQuery().processInstanceBusinessKey(businessKey, true)
				.messageEventSubscriptionName(messageEvent).singleResult();
		if (e == null) {
			throw new NotFoundException("Es wurde keine Execution fuer das Message Event '" + messageEvent
					+ "' und fuer den Business Key '" + businessKey + "' gefunden");
		}
		LOG.debug("Setze Prozessvariable '{}' mit dem Wert '{}' fuer den Business Key '{}'", variableName, value,
				businessKey);
		this.runtimeService.setVariable(e.getId(), variableName, value);
	}

	public void setProcessVariable(String executionId, String variableName, Object variableValue) {
		this.runtimeService.setVariable(executionId, variableName, variableValue);
	}

	public Object findProcessVariable(String executionId, String variableName) {
		return this.runtimeService.getVariable(executionId, variableName);
	}

	public <T> T findProcessVariable(String executionId, String variableName, Class<T> variableClass) {
		return this.runtimeService.getVariable(executionId, variableName, variableClass);
	}

}
