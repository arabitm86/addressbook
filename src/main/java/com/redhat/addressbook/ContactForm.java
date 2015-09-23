package com.redhat.addressbook;

import java.io.IOException;
import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.redhat.addressbook.backend.Contact;
import com.redhat.addressbook.backend.ContactService;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;

/* Create custom UI Components.
 *
 * Create your own Vaadin components by inheritance and composition.
 * This is a form component inherited from VerticalLayout. Use
 * Use BeanFieldGroup to bind data fields from DTO to UI fields.
 * Similarly named field by naming convention or customized
 * with @PropertyId annotation.
 */
public class ContactForm extends FormLayout implements ClickListener {

    Button save = new Button("Save", this);
    Button cancel = new Button("Cancel", this);
    TextField firstName = new TextField("First name");
    TextField lastName = new TextField("Last name");
    TextField phone = new TextField("Phone");
    TextField email = new TextField("Email");
    DateField birthDate = new DateField("Birth date");

    Contact contact;

    
    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<Contact> formFieldBindings;

    public ContactForm() {
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        /*
         * Highlight primary actions.
         * 
         * With Vaadin built-in styles you can highlight the primary save button
         * and give it a keyboard shortcut for a better UX.
         */
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        setVisible(false);
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);

        addComponents(actions, firstName, lastName, phone, email, birthDate);
    }

    void edit(Contact contact) {
        this.contact = contact;
       
        if (contact != null) {
            // Bind the properties of the contact POJO to fiels in this form
            formFieldBindings = BeanFieldGroup
                    .bindFieldsBuffered(contact, this);
            firstName.focus();
        }
        setVisible(contact != null);
    }

    @Override
    public AddressbookUI getUI() {
        return (AddressbookUI) super.getUI();
    }

    @Override
    public void buttonClick(ClickEvent event) {
    	String msg = "saved";
    	boolean update = false;
    	
        if (event.getButton() == save) {
            try {
                // Commit the fields from UI to DAO
                formFieldBindings.commit();

               
               //go to the database and get the ID of the contact selected
                ContactService c = ContactService.createDemoService();
            	ArrayList<Contact> contacts = (ArrayList<Contact>) c.findAll(null);
            	
            	
            	for (int i = 0; i < contacts.size(); i++){
            		if (contacts.get(i).getFirstName().equals(this.contact.getFirstName()) && 
            				contacts.get(i).getLastName().equals(this.contact.getLastName() )){
            			
            			this.contact.setId(contacts.get(i).getId());
            			update = true;
            			
            			break;
            		}
            	}
                
                
                // Save DAO to backend with direct synchronous service API
               
                int status = getUI().service.save(this.contact, update);

                if (status == 1 || status == 0){
                msg = String.format("Saved '%s %s'.",
                        contact.getFirstName(), contact.getLastName());
                }
                else
                	 msg = String.format("Cannot Save Duplicate '%s %s'.",
                            contact.getFirstName(), contact.getLastName());
                
                Notification.show(msg, Type.TRAY_NOTIFICATION);
                getUI().refreshContacts();
            } catch (FieldGroup.CommitException e) {
                // Validation exceptions could be shown here
            }
        } else if (event.getButton() == cancel) {
            // Place to call business logic.
            Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
            getUI().contactList.select(null);
        }

    }

}
