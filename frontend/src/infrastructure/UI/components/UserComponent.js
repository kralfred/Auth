// infrastructure/UI/components/UserComponent.js
import { UserSettingsModal } from './UserSettingsModal.js';
import { PermissionField } from './modal/PermissionField.js'; // Import your field generator
import { PermissionConverter } from './PermissionsConverter.js';
import { PERMISSION_CONFIG } from '../config/PermissionConfig.js';
import { PermissionModifier } from './PermissionModifier.js';
import { StyleLoader } from '../utils/StyleLoader.js';
import { UniversalForm } from './global/UniversalForm.js';


export class UserComponent {
    constructor(userData, userRepo) {
        StyleLoader.load('src/infrastructure/UI/styles/UserComponent.css');
        StyleLoader.load('src/infrastructure/UI/styles/UserSettingsModal.css');

        this.userData = userData;
        this.userRepo = userRepo; 
    }

    async handleEditClick() {
        try {
            // 1. Convert User Data to Universal Form fields
            const fields = PermissionConverter.toFormFields(this.userData);

            // 2. Define what happens on Save
            const onSave = async (updates) => {
                // Hits @PutMapping("/{id}/secure")
                const updatedUser = await this.userRepo.updateSecureSettings(this.userData.id, updates);
                this.userData = updatedUser; // Sync local state
                alert("Global settings updated!");
                overlay.remove();
            };

            // 3. Render Form inside an Overlay
            const formInstance = new UniversalForm(fields, onSave);
            const overlay = this.#createModalOverlay(`Global Settings: ${this.userData.username}`);
            
            overlay.querySelector(".modal-body").appendChild(formInstance.render());
            document.body.appendChild(overlay);
        } catch (error) {
            console.error("Failed to open Global Settings:", error);
        }
    }

async handleModifierClick() {
    const allUsers = await this.userRepo.getAllUsers();
    
    // Define the specific attributes your system uses
    const attributes = ['Profile', 'Billing', 'Security'];

    const modifier = new PermissionModifier(
        allUsers,
        this.userData.targetPermissions,
        attributes,
        async (updates) => {
            // updates looks like: [{targetId: "...", permissions: {Profile: "VIEW", Billing: "NONE"}}, ...]
            await this.userRepo.bulkUpdatePermissions(this.userData.id, updates);
            alert("Permissions updated!");
        }
    );

    const overlay = this.#createModalOverlay(`Access Control for ${this.userData.username}`);
    overlay.querySelector('.modal-body').appendChild(modifier.render());
    document.body.appendChild(overlay);
}

    render() {
        const card = document.createElement("div");
        card.className = "user-card";
        card.innerHTML = `
            <div class="user-info">
                <span class="user-badge">${this.userData.role === 'ROLE_ADMIN' ? 'A' : 'U'}</span>
                <h3>${this.userData.username}</h3>
                <p>${this.userData.email}</p>
            </div>
            <div class="user-actions">
                <button class="edit-btn">Global Settings</button>
                <button class="modifier-btn">Target Permissions</button>
            </div>
        `;

        card.querySelector(".edit-btn").onclick = () => this.handleEditClick();
        card.querySelector(".modifier-btn").onclick = () => this.handleModifierClick();

        return card;
    }

    /**
     * Helper to create a consistent Modal Wrapper
     */
    #createModalOverlay(title) {
        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';
        overlay.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h2>${title}</h2>
                    <button class="close-x">&times;</button>
                </div>
                <div class="modal-body"></div>
            </div>
        `;

        overlay.querySelector('.close-x').onclick = () => overlay.remove();
        // Close if clicking the darkened background
        overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };
        
        return overlay;
    }
}