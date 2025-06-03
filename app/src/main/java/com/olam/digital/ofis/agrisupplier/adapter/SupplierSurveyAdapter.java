package com.olam.digital.ofis.agrisupplier.adapter;

import static com.olam.digital.ofis.Utilities.getLookupValueByLanguage;
import static com.olam.digital.ofis.Utilities.getModuleSectionByLanguage;
import static com.olam.digital.ofis.Utilities.getNamesByLanguage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.agrisupplier.listener.SupplierDialogBoxClickListener;
import com.olam.digital.ofis.agrisupplier.ui.SupplierSurveyActivity;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeTextBoxModuleQuestionsTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmTextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DecimalFilter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.NegativeDecimalFilter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.NumberInputFilter;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SupplierSurveyAdapter extends RecyclerView.Adapter<SupplierSurveyAdapter.ViewHolder> {
    Context context;
    private Resources resources;
    List<DmSectionsItem> sections;
    OFISRoomDB db;
    private SupplierDialogBoxClickListener listener;
    private String supplierId;

    public SupplierSurveyAdapter(Context context, List<DmSectionsItem> sections, Resources resources, String supplierId) {
        this.context = context;
        this.sections = sections;
        this.resources = resources;
        db = OFISRoomDB.getDbInstance(context);
        this.supplierId = supplierId;
    }

    public void setOnDialogClickListener(SupplierDialogBoxClickListener supplierDialogBoxClickListener) {
        this.listener = supplierDialogBoxClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.supplier_submodule_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DmSectionsItem dmSectionsItem = sections.get(position);
        LinearLayout questionContainer = holder.questionsContainer;

        if (dmSectionsItem.isDefault) {
            holder.sectionTitle.setVisibility(View.GONE);
            holder.sectionDescription.setVisibility(View.GONE);
        }
        else {
            holder.sectionTitle.setText(getModuleSectionByLanguage(dmSectionsItem, true));
            if (dmSectionsItem.showDescription) {
                holder.sectionDescription.setText(getModuleSectionByLanguage(dmSectionsItem, false));
            }
            else {
                holder.sectionDescription.setVisibility(View.GONE);
            }

        }

        List<DmQuestionsModule> questionsItems = db.dmQuestionsModuleDao().getDmQuestionsSubModulebySectionId(dmSectionsItem.id);
        int questionNumber = 1;
        for (DmQuestionsModule question : questionsItems) {
            bindSectionQuestions(questionContainer, question, questionNumber, dmSectionsItem.moduleId);
            questionNumber++;
        }
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        LinearLayout questionsContainer;
        TextView sectionDescription;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.tvSectionTitle);
            sectionDescription = itemView.findViewById(R.id.tvSectionDescription);
            questionsContainer = itemView.findViewById(R.id.llQuestionContainer);
        }
    }

    private void bindSectionQuestions(LinearLayout layout, DmQuestionsModule question, int questionNumber, int moduleId) {
        View view = LayoutInflater.from(context).inflate(R.layout.supplier_question_item, layout, false);
        layout.addView(view);

        TextView tvQuestionText = view.findViewById(R.id.tvQuestionText);
        EditText editText = view.findViewById(R.id.etAnswer);
        view.setTag(question);
        String questionText = getNamesByLanguage(question);

        ((SupplierSurveyActivity) context).updateViews(question.id, view);

        if (question.isCriticalQuest || question.mandate) {
            String star = "*";
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(String.valueOf((questionNumber))).append(". ").append(questionText);
            int start = builder.length();
            builder.append(" ").append(star);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
            builder.setSpan(colorSpan, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvQuestionText.setText(builder);
        }
        else {
            tvQuestionText.setText(questionNumber + ". " + questionText);
        }


        if (question.displayType.equals("text box")) {
            renderTextBoxQuestion(view, question, moduleId);
        }
        else {
            renderDropDownQuestion(view, question, moduleId);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private void renderTextBoxQuestion(View view, DmQuestionsModule question, int moduleId) {
        EditText editText = view.findViewById(R.id.etAnswer);
        Spinner spinnerAnswer = view.findViewById(R.id.spinnerAnswer);
        TextView btnUploadEvidence = view.findViewById(R.id.tvEvidenceUpload);
        TextView tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        RelativeLayout rlEvidenceLayout = view.findViewById(R.id.rlEvidenceLayout);
        TextView tvFileName = view.findViewById(R.id.tv_evidence_name);
        ImageView deleteFile = view.findViewById(R.id.iv_evidence_delete);

        editText.setVisibility(View.VISIBLE);
        spinnerAnswer.setVisibility(View.GONE);

        int questionId = question.id;

        DmTextBoxModuleQuestions dmTextBoxModuleQuestions = db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(questionId).get(0);
        double minValue = dmTextBoxModuleQuestions.minimumRange;
        double maxValue = dmTextBoxModuleQuestions.maximumRange;
        final DmRangeTextBoxModuleQuestionsTable[] dmRangeTextBoxModuleQuestionsTable = {null};

        final boolean[] userIsInteracting = {false};
        editText.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    userIsInteracting[0] = true;
                }
                return false;
            }
        });

        if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(questionId).get(0).isAllowNegative && db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(questionId).get(0).isAllowDecimal) {
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
            NegativeDecimalFilter negativeDecimalFilter = new NegativeDecimalFilter(minValue, maxValue);
            editText.setHint(String.format(context.getString(R.string.dm_textbox_range_decimal), minValue, maxValue));
            editText.setFilters(new InputFilter[] { negativeDecimalFilter });

        }
        else if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(questionId).get(0).isAllowNegative) {
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
            editText.setHint(String.format(context.getString(R.string.dm_textbox_range_integer), (int) minValue, (int) maxValue));
            NumberInputFilter numberFilter = new NumberInputFilter(minValue, maxValue);
            editText.setFilters(new InputFilter[]{numberFilter});
        }
        else if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(questionId).get(0).isAllowDecimal) {
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            DecimalFilter decimalFilter = new DecimalFilter(minValue, maxValue, editText, true, context);
            editText.setHint(String.format(context.getString(R.string.dm_textbox_range_decimal), minValue, maxValue));
            editText.setFilters(new InputFilter[]{decimalFilter});

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    String input = editText.getText().toString();

                    if (userIsInteracting[0]) {
                        db.dmComplaintFileTableDao().deleteByQuestionId(questionId, supplierId);
                        btnUploadEvidence.setVisibility(View.GONE);
                        rlEvidenceLayout.setVisibility(View.GONE);
                        userIsInteracting[0] = false;
                    }

                    if (!input.isEmpty()) {
                        double number = Double.parseDouble(input);
                        tvErrorMessage.setVisibility(View.GONE);
                        editText.setBackgroundResource(R.drawable.answer_text_survey);
                        if (number >= minValue && number <= maxValue) {
                            editText.setError(null);
                            dmRangeTextBoxModuleQuestionsTable[0] = db.dmRangeTextBoxModuleQuestionsTableDao().getDmRangeTextBoxModuleQuestionsTableByAnswer(questionId, number);
                            btnUploadEvidence.setVisibility(dmRangeTextBoxModuleQuestionsTable[0].isCaptureProof ? View.VISIBLE : View.GONE);

                        } else {
                            editText.setError(String.format(context.getString(R.string.minimum_range_error), minValue));
                            btnUploadEvidence.setVisibility(View.GONE);
                        }
                    }
                    else {
                        rlEvidenceLayout.setVisibility(View.GONE);
                        btnUploadEvidence.setVisibility(View.GONE);
                    }
                }
            });
        }
        else {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setHint(String.format(context.getString(R.string.dm_textbox_range_integer), (int) minValue, (int) maxValue));
            InputFilter numberFilter = new DecimalFilter(minValue, maxValue, editText, false, context);
            editText.setFilters(new InputFilter[]{numberFilter});

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    String input = editText.getText().toString();

                    if (userIsInteracting[0]) {
                        db.dmComplaintFileTableDao().deleteByQuestionId(questionId, supplierId);
                        btnUploadEvidence.setVisibility(View.GONE);
                        rlEvidenceLayout.setVisibility(View.GONE);
                        userIsInteracting[0] = false;
                    }

                    if (!input.isEmpty()) {
                        tvErrorMessage.setVisibility(View.GONE);
                        editText.setBackgroundResource(R.drawable.answer_text_survey);
                        int number = Integer.parseInt(input);
                        if (number >= minValue && number <= maxValue) {
                            editText.setError(null);

                            dmRangeTextBoxModuleQuestionsTable[0] = db.dmRangeTextBoxModuleQuestionsTableDao().getDmRangeTextBoxModuleQuestionsTableByAnswer(questionId, number);
                            btnUploadEvidence.setVisibility(dmRangeTextBoxModuleQuestionsTable[0].isCaptureProof ? View.VISIBLE : View.GONE);

                        } else {
                            editText.setError(String.format(context.getString(R.string.minimum_range_error), minValue));
                            btnUploadEvidence.setVisibility(View.GONE);
                        }
                    }
                    else {
                        rlEvidenceLayout.setVisibility(View.GONE);
                        btnUploadEvidence.setVisibility(View.GONE);
                    }
                }
            });
        }

        DmSurveyAnswerTable dmSurveyAnswerTable = db.dmSurveyAnswerTableDao().getAnswerByQuestionsId(questionId, supplierId, moduleId);
        if (dmSurveyAnswerTable != null) {
            editText.setText(dmSurveyAnswerTable.answer);
        }

        DmComplaintFileTable dmComplaintFileTable = db.dmComplaintFileTableDao().getFileByQuestionId(questionId, supplierId);
        if (dmComplaintFileTable != null) {
            tvFileName.setText(dmComplaintFileTable.getFile_name());
            rlEvidenceLayout.setVisibility(View.VISIBLE);
            btnUploadEvidence.setVisibility(View.GONE);

            deleteFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    evidenceDeleteConfirmationPopUp(tvFileName, rlEvidenceLayout, btnUploadEvidence, questionId);
                }
            });
        }

        btnUploadEvidence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUploadEvidence.setBackgroundResource(R.drawable.dm_captureproof_bg);
                if (btnUploadEvidence.getVisibility() == View.VISIBLE) {
                    captureEvidence(questionId, dmRangeTextBoxModuleQuestionsTable[0].proofFormat, view);
                }
            }
        });
    }


    private void renderDropDownQuestion(View view, DmQuestionsModule question, int moduleId) {
        int questionId = question.id;

        EditText editText = view.findViewById(R.id.etAnswer);
        Spinner spinner = view.findViewById(R.id.spinnerAnswer);
        TextView btnUploadEvidence = view.findViewById(R.id.tvEvidenceUpload);
        TextView tvFileName = view.findViewById(R.id.tv_evidence_name);
        RelativeLayout rlEvidenceLayout = view.findViewById(R.id.rlEvidenceLayout);
        ImageView deleteFile = view.findViewById(R.id.iv_evidence_delete);
        TextView tvErrorMessage = view.findViewById(R.id.tvErrorMessage);

        editText.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
        spinner.setTag(new DmLookupValuesItem());

        List<DmLookupValuesItem> dmLookupValuesISubmoduleList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(question.id);
        DmSurveyAnswerTable dmSurveyAnswerTable = db.dmSurveyAnswerTableDao().getAnswerByQuestionsId(questionId, supplierId, moduleId);
        final String[] proofFormat = {""};

        List<String> dropDownValueNames = new ArrayList<>();
        dropDownValueNames.add(resources.getString(R.string.ModuleAnswerHint));

        int previousSelectedAnswer = 0;

        for (int k = 0; k < dmLookupValuesISubmoduleList.size(); k++) {
            DmLookupValuesItem dmLookupValuesItem = dmLookupValuesISubmoduleList.get(k);
            dropDownValueNames.add(getLookupValueByLanguage(dmLookupValuesItem));

            if (dmSurveyAnswerTable != null && dmSurveyAnswerTable.answerID.equalsIgnoreCase(String.valueOf(dmLookupValuesItem.id))) {
                previousSelectedAnswer = k + 1;
                proofFormat[0] = dmLookupValuesItem.proofFormat;
                spinner.setTag(dmLookupValuesItem);
            }
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, R.id.ofis_spinner_text_view, dropDownValueNames);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dm);
        spinner.setAdapter(spinnerAdapter);

        final boolean[] userIsInteracting = {false};
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    userIsInteracting[0] = true;
                }
                return false;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tvErrorMessage.setVisibility(View.GONE);
                spinner.setBackgroundResource(R.drawable.spinner_bg_light);
                if (userIsInteracting[0]) {
                    db.dmComplaintFileTableDao().deleteByQuestionId(questionId, supplierId);
                    btnUploadEvidence.setVisibility(View.GONE);
                    proofFormat[0] = "";
                    rlEvidenceLayout.setVisibility(View.GONE);
                    spinner.setTag(new DmLookupValuesItem());
                }

                if (position > 0) {
                    DmLookupValuesItem dmLookupValuesItem = dmLookupValuesISubmoduleList.get(position - 1);
                    spinner.setTag(dmLookupValuesItem);

                    if (dmLookupValuesItem.isCaptureProof && userIsInteracting[0]) {
                        proofFormat[0] = dmLookupValuesItem.proofFormat;
                        btnUploadEvidence.setVisibility(View.VISIBLE);
                        userIsInteracting[0] = false;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (previousSelectedAnswer > 0) {
            spinner.setSelection(previousSelectedAnswer);
        }

        DmComplaintFileTable dmComplaintFileTable = db.dmComplaintFileTableDao().getFileByQuestionId(questionId, supplierId);
        if (dmComplaintFileTable != null) {
            tvFileName.setText(dmComplaintFileTable.getFile_name());
            rlEvidenceLayout.setVisibility(View.VISIBLE);
            btnUploadEvidence.setVisibility(View.GONE);


            deleteFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    evidenceDeleteConfirmationPopUp(tvFileName, rlEvidenceLayout, btnUploadEvidence, questionId);
                }
            });
        }

        btnUploadEvidence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureEvidence(questionId, proofFormat[0], view);
            }
        });
    }

    private void evidenceDeleteConfirmationPopUp(TextView tvFileName, RelativeLayout rlEvidenceLayout, TextView btnUploadEvidence, int questionId) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder((Activity) context);
        builder1.setMessage(resources.getString(R.string.dm_do_you_want_to_remove_this_report));
        builder1.setTitle(resources.getString(R.string.Alert_txt));
        builder1.setCancelable(false);
        builder1.setPositiveButton(resources.getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        tvFileName.setText("");
                        rlEvidenceLayout.setVisibility(View.GONE);
                        btnUploadEvidence.setVisibility(View.VISIBLE);
                        db.dmComplaintFileTableDao().deleteByQuestionId(questionId, supplierId);
                    }
                }
        );

        builder1.setNegativeButton(resources.getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void captureEvidence(int id, String proofFormat, View view) {
        CharSequence[] options = new CharSequence[0];

        try {
            if (Objects.equals(proofFormat, "Both")) {
                options = new CharSequence[]{resources.getString(R.string.take_photo), resources.getString(R.string.dm_choose_from_gallery), resources.getString(R.string.cancel)};
            } else if (Objects.equals(proofFormat, "Document (.doc, .docx, .xls, .xlsx, .pdf, .csv)")) {
                options = new CharSequence[]{ resources.getString(R.string.dm_choose_from_gallery), resources.getString(R.string.cancel)};
            } else if (Objects.equals(proofFormat, "Image (.png, .jpg, .jpeg)")) {
                options = new CharSequence[]{resources.getString(R.string.take_photo),  resources.getString(R.string.dm_choose_from_gallery), resources.getString(R.string.cancel)};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        CharSequence[] finalOptions = options;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(resources.getString(R.string.dm_add_photo));

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (finalOptions[item].equals(resources.getString(R.string.cancel))) {
                    dialog.dismiss();
                }
                else {
                    listener.onDialogItemClick(view, item, id, proofFormat, finalOptions);
                }
            }
        });
        builder.show();
    }

}
