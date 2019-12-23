<?xml version="1.0" encoding="UTF-8"?>
<xmi:XMI xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ird="http://studio.genesyslab.com/ird/" xmlns:notation="http://www.eclipse.org/gmf/runtime/1.0.2/notation">
  <ird:StrategyDiagram xmi:id="_33D0EIoaEeG_nt9_QYQUcg" name="default" designedUsing="Composer 8.1.400.36">
    <history>8.1.0</history>
    <history>8.1.300.01</history>
    <blocks xsi:type="ird:EntryBlock" xmi:id="_6e2LkNz6EeKyG9aF7VXEXg" name="Entry1" starting="true" category="Entry">
      <variables xmi:id="_6e-ucNz6EeKyG9aF7VXEXg" name="system.BaseURL" value="getBaseURL()" description="Base URL" type="System"/>
      <variables xmi:id="_6e-ucdz6EeKyG9aF7VXEXg" name="system.RelativePathURL" value="getRelativePathURL()" description="Relative path" type="System"/>
      <variables xmi:id="_6fBxwNz6EeKyG9aF7VXEXg" name="system.Language" value="'en-US'" description="Application Language" type="System"/>
      <variables xmi:id="_6fBxwdz6EeKyG9aF7VXEXg" name="system.InteractionID" value="system.StartEvent.data.interactionid" description="The current interaction ID." type="System"/>
      <variables xmi:id="_6fBxwtz6EeKyG9aF7VXEXg" name="system.CallID" value="_genesys.ixn.interactions[system.InteractionID].voice.callid" description="callid created by the switch." type="System"/>
      <variables xmi:id="_6fCY0Nz6EeKyG9aF7VXEXg" name="system.DNIS" value="_genesys.ixn.interactions[system.InteractionID].voice.dnis" description="DNIS associated with Called phone number" type="System"/>
      <variables xmi:id="_6fCY0dz6EeKyG9aF7VXEXg" name="system.ThisDN" value="system.StartEvent.data.focusdeviceid" description="ThisDN attribute of last point of presence for this call" type="System"/>
      <variables xmi:id="_6fCY0tz6EeKyG9aF7VXEXg" name="system.ANI" value="_genesys.ixn.interactions[system.InteractionID].voice.ani" description="ANI associated with the calling party." type="System"/>
      <variables xmi:id="_6fCY09z6EeKyG9aF7VXEXg" name="system.StartEvent" value="undefined" description="The content of the specified start event" type="System"/>
      <variables xmi:id="_6fCY1Nz6EeKyG9aF7VXEXg" name="system.LastErrorEvent" value="'undefined'" description="Last error" type="System"/>
      <variables xmi:id="_6fCY1dz6EeKyG9aF7VXEXg" name="system.LastErrorEventName" value="'undefined'" description="Last error event name" type="System"/>
      <variables xmi:id="_6fCY1tz6EeKyG9aF7VXEXg" name="system.LastErrorDescription" value="'undefined'" description="Last error description" type="System"/>
      <variables xmi:id="_6fCY19z6EeKyG9aF7VXEXg" name="system.WebServiceStubbing" value="'0'" description="Flag to control WebServices Stubbing. '1' - ON" type="System"/>
      <variables xmi:id="_6fCY2Nz6EeKyG9aF7VXEXg" name="system.TerminateIxnOnExit" value="1" description="Flag to control if Exit block should terminate multimedia interactions. '1' - ON" type="System"/>
      <variables xmi:id="_6fCY2dz6EeKyG9aF7VXEXg" name="system.TenantID" value="parseInt(_genesys.ixn.interactions[system.InteractionID].tenantid)" description="The current Tenant ID." type="System"/>
      <variables xmi:id="_6fCY2tz6EeKyG9aF7VXEXg" name="system.TenantName" value="_genesys.session.tenant" description="The current Tenant name." type="System"/>
      <variables xmi:id="_6fCY29z6EeKyG9aF7VXEXg" name="system.LastTargetComponentSelected" value="'undefined'" description="Target to which the Interaction was routed definitively." type="System"/>
      <variables xmi:id="_6fCY3Nz6EeKyG9aF7VXEXg" name="system.LastTargetObjectSelected" value="'undefined'" description="High-level Target to which the Interaction was routed definitively" type="System"/>
      <variables xmi:id="_6fCY3dz6EeKyG9aF7VXEXg" name="system.LastTargetSelected" value="'undefined'" description="DN and the Switch name of the Target to which the Interaction was routed definitively" type="System"/>
      <variables xmi:id="_6fCY3tz6EeKyG9aF7VXEXg" name="system.LastVirtualQueueSelected" value="'undefined'" description="The Alias of the Virtual Queue specified in the target list to which the target where the interaction was routed belongs" type="System"/>
      <variables xmi:id="_6fCY39z6EeKyG9aF7VXEXg" name="system.LastSubmitRequestId" value="'undefined'" description="Requestid  value of the Last queue:submit execution" type="System"/>
      <variables xmi:id="_6fCY4Nz6EeKyG9aF7VXEXg" name="system.OPM" value="getOPMParameters()" description="Operational Parameters Data Variable" type="System"/>
      <variables xmi:id="_6fCY4dz6EeKyG9aF7VXEXg" name="system.OCS_RecordURI" value="getWorkflowRecordURI()" description="OCS Record URI" type="System"/>
      <variables xmi:id="_6fCY4tz6EeKyG9aF7VXEXg" name="system.OCS_URI" value="getWorkflowOCSURI()" description="OCS URI" type="System"/>
      <variables xmi:id="_6fCY49z6EeKyG9aF7VXEXg" name="system.OCS_Record" value="getWorkflowOCSRecord()" description="OCS Record" type="System"/>
      <variables xmi:id="_6fCY5Nz6EeKyG9aF7VXEXg" name="system.ParentInteractionID" value="_genesys.ixn.interactions[system.InteractionID].parentid" description="The current interaction parent ID." type="System"/>
      <variables xmi:id="_6fCY5dz6EeKyG9aF7VXEXg" name="system.OriginatingSession" value="undefined" description="The originating session context." type="System"/>
      <variables name="system.InteractionUID" value="_genesys.ixn.interactions[system.InteractionID].g_uid" description="The globally unique ID for the interaction that is defined by the underlying media system." type="System"/>
      <variables name="system.InitialInteractionID" value="system.StartEvent.data.interactionid" description="The ID of the interaction that started this session." type="System"/>
      <variables name="system.CurrentQueue" value="_genesys.ixn.interactions[system.InteractionID].msgbased.queue" description="queue attribute for this interaction." type="System"/>
      <variables name="system.InteractionMediaType" value="undefined" description="The originating media type of the interaction." type="System"/>
      <variables name="system.InteractionType" value="undefined" description="The origin type of the interaction." type="System"/>
      <variables name="system.InteractionSubType" value="undefined" description="The origin sub-type of the interaction." type="System"/>
      <variables name="system.SubmittedBy" value="_genesys.ixn.interactions[system.InteractionID].location.media_server" description="This is the originating media type of the interaction." type="System"/>
      <variables name="system.ExternalID" value="undefined" description="This is the ID of the interaction that has been assigned by the originating media server." type="System"/>
    </blocks>
    <blocks xsi:type="ird:ExitBlock" xmi:id="_6niNgNz6EeKyG9aF7VXEXg" name="Exit1" terminating="true" category="Exit"/>
    <links xsi:type="ird:WorkflowOutputLink" xmi:id="_7uvtMNz6EeKyG9aF7VXEXg" fromBlock="_6e2LkNz6EeKyG9aF7VXEXg" toBlock="_6niNgNz6EeKyG9aF7VXEXg"/>
    <namespaces xmi:id="_5WEloNz6EeKyG9aF7VXEXg" name="ws" value="http://www.genesyslab.com/modules/ws"/>
    <namespaces xmi:id="_5WElodz6EeKyG9aF7VXEXg" name="queue" value="http://www.genesyslab.com/modules/queue"/>
    <namespaces xmi:id="_5WElotz6EeKyG9aF7VXEXg" name="dialog" value="http://www.genesyslab.com/modules/dialog"/>
    <namespaces xmi:id="_5WElo9z6EeKyG9aF7VXEXg" name="session" value="http://www.genesyslab.com/modules/session"/>
    <namespaces xmi:id="_5WElpNz6EeKyG9aF7VXEXg" name="ixn" value="http://www.genesyslab.com/modules/interaction"/>
    <namespaces xmi:id="_5WElpdz6EeKyG9aF7VXEXg" name="classification" value="http://www.genesyslab.com/modules/classification"/>
  </ird:StrategyDiagram>
  <notation:Diagram xmi:id="_33D0EYoaEeG_nt9_QYQUcg" type="Ird" element="_33D0EIoaEeG_nt9_QYQUcg" name="default.workflow" measurementUnit="Pixel">
    <children xsi:type="notation:Shape" xmi:id="_6fiIENz6EeKyG9aF7VXEXg" type="1001" element="_6e2LkNz6EeKyG9aF7VXEXg">
      <children xsi:type="notation:DecorationNode" xmi:id="_6fj9QNz6EeKyG9aF7VXEXg" type="6003"/>
      <children xsi:type="notation:DecorationNode" xmi:id="_6fj9Qdz6EeKyG9aF7VXEXg" type="6001"/>
      <layoutConstraint xsi:type="notation:Bounds" xmi:id="_6fiIEdz6EeKyG9aF7VXEXg" x="400" y="100"/>
    </children>
    <children xsi:type="notation:Shape" xmi:id="_6nkCsNz6EeKyG9aF7VXEXg" type="1002" element="_6niNgNz6EeKyG9aF7VXEXg">
      <children xsi:type="notation:DecorationNode" xmi:id="_6nkCstz6EeKyG9aF7VXEXg" type="6002"/>
      <children xsi:type="notation:DecorationNode" xmi:id="_6nkCs9z6EeKyG9aF7VXEXg" type="4001"/>
      <layoutConstraint xsi:type="notation:Bounds" xmi:id="_6nkCsdz6EeKyG9aF7VXEXg" x="400" y="250"/>
    </children>
    <styles xsi:type="notation:DiagramStyle" xmi:id="_33D0EooaEeG_nt9_QYQUcg"/>
    <edges xsi:type="notation:Connector" xmi:id="_7uywgNz6EeKyG9aF7VXEXg" type="3001" element="_7uvtMNz6EeKyG9aF7VXEXg" source="_6fiIENz6EeKyG9aF7VXEXg" target="_6nkCsNz6EeKyG9aF7VXEXg" roundedBendpointsRadius="10" routing="Rectilinear" closestDistance="true" lineColor="16711680">
      <children xsi:type="notation:DecorationNode" xmi:id="_7uzXkNz6EeKyG9aF7VXEXg" type="5001">
        <layoutConstraint xsi:type="notation:Location" xmi:id="_7uzXkdz6EeKyG9aF7VXEXg" x="5" y="5"/>
      </children>
      <styles xsi:type="notation:FontStyle" xmi:id="_7uywgdz6EeKyG9aF7VXEXg"/>
      <bendpoints xsi:type="notation:RelativeBendpoints" xmi:id="_7uywgtz6EeKyG9aF7VXEXg" points="[4, 0, 0, -100]$[4, 100, 0, 0]"/>
      <sourceAnchor xsi:type="notation:IdentityAnchor" xmi:id="_7vKj8Nz6EeKyG9aF7VXEXg" id="(0.4818181818181818,1.0)"/>
      <targetAnchor xsi:type="notation:IdentityAnchor" xmi:id="_7vKj8dz6EeKyG9aF7VXEXg" id="(0.5181818181818182,0.0)"/>
    </edges>
  </notation:Diagram>
</xmi:XMI>
