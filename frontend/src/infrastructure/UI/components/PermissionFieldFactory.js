// infrastructure/UI/components/PermissionFieldFactory.js

export const PermissionFields = {
    // A simple toggle for boolean permissions
    CHECKBOX: (name, value, label) => `
        <div class="perm-field">
            <input type="checkbox" name="perm_${name}" ${value ? 'checked' : ''}>
            <span>${label}</span>
        </div>`,

    TEXT: (name, value, label) => `
        <div class="perm-field">
            <label>${label}</label>
            <input type="text" name="perm_${name}" value="${value || ''}" placeholder="Enter value...">
        </div>`,
    
    // A dropdown for tiered permissions
    SELECT: (name, value, label, options) => `
        <div class="perm-field">
            <label>${label}</label>
            <select name="perm_${name}">
                ${options.map(opt => `<option value="${opt}" ${value === opt ? 'selected' : ''}>${opt}</option>`).join('')}
            </select>
        </div>`
};