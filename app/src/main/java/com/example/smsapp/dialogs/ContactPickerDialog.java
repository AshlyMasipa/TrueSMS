package com.example.smsapp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smsapp.R;
import com.example.smsapp.adapters.ContactAdapter;
import com.example.smsapp.models.Contact;
import com.example.smsapp.utils.ContactUtils;
import java.util.ArrayList;
import java.util.List;

public class ContactPickerDialog extends DialogFragment {

    private ContactSelectedListener listener;
    private List<Contact> allContacts;
    private ContactAdapter adapter;

    public interface ContactSelectedListener {
        void onContactSelected(Contact contact);
    }

    public static ContactPickerDialog newInstance() {
        return new ContactPickerDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (getParentFragment() != null) {
                listener = (ContactSelectedListener) getParentFragment();
            } else {
                listener = (ContactSelectedListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ContactSelectedListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Inflate custom layout
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_contact_picker, null);

        RecyclerView recyclerView = view.findViewById(R.id.contacts_recycler_view);
        SearchView searchView = view.findViewById(R.id.search_view);

        // Load contacts
        allContacts = ContactUtils.getAllContacts(requireContext());
        adapter = new ContactAdapter(allContacts, this::onContactSelected);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterContacts(newText);
                return true;
            }
        });

        builder.setView(view)
                .setTitle("Select Contact")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    private void filterContacts(String query) {
        List<Contact> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(allContacts);
        } else {
            for (Contact contact : allContacts) {
                if (contact.getName().toLowerCase().contains(query.toLowerCase()) ||
                        contact.getPhoneNumber().contains(query)) {
                    filtered.add(contact);
                }
            }
        }
        adapter.updateContacts(filtered);
    }

    private void onContactSelected(Contact contact) {
        if (listener != null) {
            listener.onContactSelected(contact);
        }
        dismiss();
    }
}