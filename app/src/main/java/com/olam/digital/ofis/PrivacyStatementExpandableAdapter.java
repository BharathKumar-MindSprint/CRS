package com.olam.digital.ofis;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PrivacyStatementExpandableAdapter extends BaseExpandableListAdapter {

        private Activity context;
        private List<String> group_list;

        public PrivacyStatementExpandableAdapter(Activity context, List<String> group_list) {
            this.context = context;
            this.group_list = group_list;
        }

        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
                switch (groupPosition) {
                    case 0:
                        convertView = inflater.inflate(R.layout.privacy_statement_general_principle, null);
                        break;
                    case 1:
                        convertView = inflater.inflate(R.layout.privacy_statement_personal_information, null);
                        break;
                    case 2:
                        convertView = inflater.inflate(R.layout.privacy_statement_cookies, null);
                        break;
                    case 3:
                        convertView = inflater.inflate(R.layout.privacy_statement_how_ofi_uses_personal_info, null);
                        break;
                    case 4:
                        convertView = inflater.inflate(R.layout.privacy_statement_when_we_share, null);
                        break;
                    case 5:
                        convertView = inflater.inflate(R.layout.privacy_statement_cross_border, null);
                        break;
                    case 6:
                        convertView = inflater.inflate(R.layout.privacy_statement_security_of_personal_info, null);
                        break;
                    case 7:
                        convertView = inflater.inflate(R.layout.privacy_statement_retention, null);
                        break;
                    case 8:
                        convertView = inflater.inflate(R.layout.privacy_statement_children, null);
                        break;
                    case 9:
                        convertView = inflater.inflate(R.layout.privacy_statement_link_to_website, null);
                        break;
                    case 10:
                        convertView = inflater.inflate(R.layout.privacy_statement_requests_and_clarifications, null);
                        break;
                    case 11:
                        convertView = inflater.inflate(R.layout.privacy_statement_gdpr_additional_info, null);
                        break;
                    case 12:
                        convertView = inflater.inflate(R.layout.privacy_statement_additional_california, null);
                        break;
                    case 13:
                        convertView = inflater.inflate(R.layout.privacy_statement_changes_to_private_policy, null);
                        break;
                    default:
                        convertView = inflater.inflate(R.layout.privacy_statement_general_principle, null);
                        // Maybe we should implement a default behaviour but it should be ok we know there are 4 child types right?
                        break;
                }


            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            // We are showing static layout . So returning 1
            return 1;
        }

        public Object getGroup(int groupPosition) {
            return group_list.get(groupPosition);
        }

        public int getGroupCount() {
            return group_list.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            final String groupTitle = (String) getGroup(groupPosition);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.privacy_statement_group_item, null);

            } else {
                convertView = inflater.inflate(R.layout.privacy_statement_group_item, null);
                // There is nothing to do here really we just need to set the content of view which we do in both cases
            }
            TextView item = (TextView) convertView.findViewById(R.id.group_title);
            item.setTypeface(null, Typeface.BOLD);
            item.setText(groupTitle);

          ImageView imgs = (ImageView) convertView
                    .findViewById(R.id.expImageView);
          if (isExpanded){
              imgs.setImageResource(R.drawable.ic_task_drop_down_expand);
          }else{
              imgs.setImageResource(R.drawable.ic_drop_down_collapse);
          }
            return convertView;
        }


        public boolean hasStableIds() {
            return true;
        }


        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

//
//        @Override
//        public int getChildTypeCount() {
//            return 4;
//        }
//
//        @Override
//        public int getGroupTypeCount() {
//            return 3;
//        }
    }
