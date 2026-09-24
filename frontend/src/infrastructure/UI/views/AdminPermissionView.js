
export class AdminPermissionView {
  constructor(adminService, entityService) {
    this.adminService = adminService;
    this.entityService = entityService;
    this.selectedEntity = null;
    this.entities = [];
  }

  render() {
    const mainContainer = document.createElement("div");
    mainContainer.style.display = "flex";
    mainContainer.style.height = "calc(100vh - 80px)";
    mainContainer.style.gap = "20px";
    mainContainer.style.padding = "20px";
    mainContainer.style.boxSizing = "border-box";
    mainContainer.style.fontFamily = "sans-serif";

    // Left Panel: Entity List
    const leftPanel = this._createPanel("Entities", "Add Entity", () => {
      this._showCreateModal("Entity", async (entityName) => {
        await this.entityService.createEntityType?.(entityName);
        await this._loadEntities(entityListContainer);
      });
    });

    const entityListContainer = document.createElement("div");
    entityListContainer.style.flex = "1";
    entityListContainer.style.overflowY = "auto";
    leftPanel.body.appendChild(entityListContainer);

    // Right Panel: Attribute List
    const rightPanel = this._createPanel("Attributes", "Add Attribute", () => {
      if (!this.selectedEntity) {
        alert("Please select an entity first!");
        return;
      }
      this._showCreateModal("Attribute", async (attrName) => {
        await this.entityService.createPermissionAttribute(attrName, this.selectedEntity.name || this.selectedEntity);
        await this._loadAttributes(attributeListContainer, this.selectedEntity);
      });
    });

    const attributeListContainer = document.createElement("div");
    attributeListContainer.style.flex = "1";
    attributeListContainer.style.overflowY = "auto";
    rightPanel.body.appendChild(attributeListContainer);

    // Initial state for attributes panel
    attributeListContainer.innerHTML = `<p style="color: #7f8c8d; text-align: center; margin-top: 40px;">Select an entity from the left to view its attributes.</p>`;

    mainContainer.appendChild(leftPanel.element);
    mainContainer.appendChild(rightPanel.element);

    // Load Entities
    this._loadEntities(entityListContainer, attributeListContainer);

    return mainContainer;
  }

  // Panel Component Shell
  _createPanel(titleText, buttonText, onButtonClick) {
    const panel = document.createElement("div");
    Object.assign(panel.style, {
      flex: "1",
      display: "flex",
      flexDirection: "column",
      border: "1px solid #e0e0e0",
      borderRadius: "8px",
      backgroundColor: "#ffffff",
      padding: "15px",
      boxShadow: "0 2px 5px rgba(0,0,0,0.05)"
    });

    const header = document.createElement("h3");
    header.textContent = titleText;
    header.style.margin = "0 0 15px 0";
    header.style.color = "#2c3e50";

    const body = document.createElement("div");
    body.style.flex = "1";
    body.style.display = "flex";
    body.style.flexDirection = "column";

    const footer = document.createElement("div");
    footer.style.marginTop = "15px";

    const btn = document.createElement("button");
    btn.textContent = `+ ${buttonText}`;
    Object.assign(btn.style, {
      width: "100%",
      padding: "10px",
      backgroundColor: "#3498db",
      color: "white",
      border: "none",
      borderRadius: "4px",
      cursor: "pointer",
      fontWeight: "bold"
    });
    btn.onclick = onButtonClick;

    footer.appendChild(btn);
    panel.append(header, body, footer);

    return { element: panel, body };
  }

  // Load Entities from Service
  async _loadEntities(container, attributeListContainer) {
    container.innerHTML = "<p>Loading entities...</p>";
    try {

      
      this.entities = await this.adminService.loadEntities();
      container.innerHTML = "";

      if (!this.entities || this.entities.length === 0) {
        container.innerHTML = "<p>No entities found.</p>";
        return;
      }
      if(this.entities == undefined){
        this.entities = []
      }

      this.entities.forEach((entity) => {
        const item = document.createElement("div");
        const entityName = typeof entity === "string" ? entity : entity.name || entity.id;

        item.textContent = entityName;
        Object.assign(item.style, {
          padding: "12px 15px",
          marginBottom : "8px",
          border : "1px solid #ecf0f1",
          borderRadius : "4px",
          cursor : "pointer",
          backgroundColor : "#f8f9fa",
          transition : "all 0.2s"
        });

        item.onclick = () => {
          this.selectedEntity = entity;
          // Highlight selected
          Array.from(container.children).forEach(child => child.style.backgroundColor = "#f8f9fa");
          item.style.backgroundColor = "#e8f4f8";
          item.style.borderColor = "#3498db";

          if (attributeListContainer) {
            this._loadAttributes(attributeListContainer, entity);
          }
        };

        container.appendChild(item);
      });
    } catch (e) {
      container.innerHTML = `<p style="color: red;">Failed to load entitiesss: ${e.message}</p>`;
    }
  }

  // Load Attributes for Selected Entity
  async _loadAttributes(container, entity) {
    container.innerHTML = "<p>Loading attributes...</p>";
    try {
      const entityName = typeof entity === "string" ? entity : entity.name || entity.id;
      const attributes = await this.entityService.getEntityById("attributes", entityName);
      
      container.innerHTML = "";

      const attrList = Array.isArray(attributes) ? attributes : attributes.attributes || [];

      if (attrList.length === 0) {
        container.innerHTML = `<p style="color: #7f8c8d;">No attributes found for ${entityName}.</p>`;
        return;
      }

      attrList.forEach((attr) => {
        const item = document.createElement("div");
        item.textContent = typeof attr === "string" ? attr : attr.name || attr.attributeName;
        Object.assign(item.style, {
          padding: "10px 12px",
          marginBottom: "6px",
          backgroundColor: "#ffffff",
          border: "1px solid #e2e8f0",
          borderRadius: "4px"
        });
        container.appendChild(item);
      });
    } catch (e) {
      container.innerHTML = `<p style="color: red;">Failed to load attributes: ${e.message}</p>`;
    }
  }

  // Universal Creation Modal
  _showCreateModal(typeLabel, onSubmit) {
    const overlay = document.createElement("div");
    Object.assign(overlay.style, {
      position: "fixed",
      top: "0",
      left: "0",
      width: "100vw",
      height: "100vh",
      backgroundColor: "rgba(0,0,0,0.5)",
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      zIndex: "1000"
    });

    const modal = document.createElement("div");
    Object.assign(modal.style, {
      backgroundColor: "white",
      padding: "25px",
      borderRadius: "8px",
      width: "350px",
      boxShadow: "0 4px 15px rgba(0,0,0,0.2)"
    });

    const title = document.createElement("h3");
    title.textContent = `Create New ${typeLabel}`;
    title.style.margin = "0 0 15px 0";

    const input = document.createElement("input");
    input.type = "text";
    input.placeholder = `${typeLabel} Name`;
    Object.assign(input.style, {
      width: "100%",
      padding: "10px",
      boxSizing: "border-box",
      marginBottom: "20px",
      border: "1px solid #ccc",
      borderRadius: "4px"
    });

    const actions = document.createElement("div");
    actions.style.display = "flex";
    actions.style.justifyContent = "flex-end";
    actions.style.gap = "10px";

    const cancelBtn = document.createElement("button");
    cancelBtn.textContent = "Cancel";
    Object.assign(cancelBtn.style, {
      padding: "8px 15px",
      backgroundColor: "#e74c3c",
      color: "white",
      border: "none",
      borderRadius: "4px",
      cursor: "pointer"
    });
    cancelBtn.onclick = () => document.body.removeChild(overlay);

    const submitBtn = document.createElement("button");
    submitBtn.textContent = "Create";
    Object.assign(submitBtn.style, {
      padding: "8px 15px",
      backgroundColor: "#2ecc71",
      color: "white",
      border: "none",
      borderRadius: "4px",
      cursor: "pointer"
    });

    submitBtn.onclick = async () => {
      const val = input.value.trim();
      if (!val) return;

      submitBtn.disabled = true;
      submitBtn.textContent = "Saving...";

      try {
        await onSubmit(val);
        document.body.removeChild(overlay);
      } catch (err) {
        alert(`Error creating ${typeLabel}: ${err.message}`);
        submitBtn.disabled = false;
        submitBtn.textContent = "Create";
      }
    };

    actions.append(cancelBtn, submitBtn);
    modal.append(title, input, actions);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    input.focus();
  }
}