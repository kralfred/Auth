// infrastructure/UI/views/CreatePermissionView.js
export class CreatePermissionView {
    constructor(permissionService) {
        this.permissionService = permissionService;
        this.actions = [];
        this.entities = [];
        this.attributes = [];
    }

    async render() {
        const container = document.createElement("div");
        container.className = "create-permission";

        const heading = document.createElement("h2");
        heading.textContent = "Create Permission";
        container.appendChild(heading);

        const status = document.createElement("p");
        status.textContent = "Loading…";
        container.appendChild(status);

        const form = document.createElement("form");
        form.style.display = "none";
        container.appendChild(form);

        // --- action select ---
        const actionLabel = document.createElement("label");
        actionLabel.textContent = "Action";
        const actionSelect = document.createElement("select");
        actionSelect.name = "actionId";
        actionSelect.required = true;
        actionLabel.appendChild(actionSelect);
        form.appendChild(actionLabel);

        // --- entity select ---
        const entityLabel = document.createElement("label");
        entityLabel.textContent = "Entity";
        const entitySelect = document.createElement("select");
        entitySelect.name = "entityId";
        entitySelect.required = true;
        entityLabel.appendChild(entitySelect);
        form.appendChild(entityLabel);

        // --- attributes (rendered after entity pick) ---
        const attrFieldset = document.createElement("fieldset");
        const attrLegend = document.createElement("legend");
        attrLegend.textContent = "Attributes (optional)";
        attrFieldset.appendChild(attrLegend);
        form.appendChild(attrFieldset);

        // --- name ---
        const nameLabel = document.createElement("label");
        nameLabel.textContent = "Permission name";
        const nameInput = document.createElement("input");
        nameInput.name = "permissionName";
        nameInput.placeholder = "CREATE_permission";
        nameInput.required = true;
        nameLabel.appendChild(nameInput);
        form.appendChild(nameLabel);

        const submit = document.createElement("button");
        submit.type = "submit";
        submit.textContent = "Create Permission";
        form.appendChild(submit);

        const message = document.createElement("p");
        form.appendChild(message);

        // --- load initial data ---
        try {
            const { actions, entities } = await this.permissionService.loadFormData();
            this.actions = actions;
            this.entities = entities;

            actionSelect.append(this._option("", "— choose an action —"));
            actions.forEach(a => actionSelect.append(this._option(a.id, a.name)));

            entitySelect.append(this._option("", "— choose an entity —"));
            entities.forEach(e => entitySelect.append(this._option(e.id, e.name)));

            status.remove();
            form.style.display = "block";
        } catch (err) {
            status.textContent = "Failed to load form data: " + err.message;
            return container;
        }

        // --- attributes load on entity change ---
        entitySelect.addEventListener("change", async () => {
            const entityId = entitySelect.value;
            attrFieldset.querySelectorAll("label").forEach(l => l.remove());
            if (!entityId) return;
            try {
                this.attributes = await this.permissionService.loadAttributes(entityId);
                if (this.attributes.length === 0) {
                    attrFieldset.append(this._text("(no attributes for this entity)"));
                } else {
                    this.attributes.forEach(a => {
                        const lbl = document.createElement("label");
                        const cb = document.createElement("input");
                        cb.type = "checkbox";
                        cb.value = a.id;
                        cb.name = "attr";
                        lbl.append(cb, document.createTextNode(" " + a.name));
                        attrFieldset.appendChild(lbl);
                    });
                }
            } catch (err) {
                attrFieldset.append(this._text("Failed to load attributes: " + err.message));
            }
        });

        // --- submit ---
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            submit.disabled = true;
            message.textContent = "";

            const attributeIds = Array.from(attrFieldset.querySelectorAll("input[type=checkbox]:checked"))
                .map(cb => cb.value);

            try {
                await this.permissionService.create({
                    name: nameInput.value.trim(),
                    actionId: actionSelect.value,
                    entityId: entitySelect.value,
                    attributeIds
                });
                message.textContent = "Permission created.";
                form.reset();
                attrFieldset.querySelectorAll("label").forEach(l => l.remove());
            } catch (err) {
                message.textContent = "Error: " + err.message;
            } finally {
                submit.disabled = false;
            }
        });

        return container;
    }

    _option(value, label) {
        const o = document.createElement("option");
        o.value = value;
        o.textContent = label;
        return o;
    }

    _text(str) {
        return document.createTextNode(str);
    }
}