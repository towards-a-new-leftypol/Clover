/*
 * Kuroba - *chan browser https://github.com/Adamantcheese/Kuroba/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.adamantcheese.chan.ui.layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.adamantcheese.chan.R;
import com.github.adamantcheese.chan.core.net.NetUtils;
import com.github.adamantcheese.chan.core.net.NetUtilsClasses;
import com.github.adamantcheese.chan.utils.BackgroundUtils;
import com.github.adamantcheese.chan.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.HttpUrl;

import static com.github.adamantcheese.chan.utils.StringUtils.applySearchSpans;

public class SelectLayout<T>
        extends LinearLayout
        implements SearchLayout.SearchLayoutCallback, View.OnClickListener {
    private RecyclerView recyclerView;
    private Button checkAllButton;

    private final List<SelectItem<T>> items = new ArrayList<>();
    private SelectAdapter adapter;
    private boolean allChecked = false;
    // "Should we select only one item?"
    private boolean selectSingle = false;

    public SelectLayout(Context context) {
        super(context);
    }

    public SelectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onSearchEntered(String entered) {
        adapter.search(entered);
    }

    @Override
    public void onClearPressedWhenEmpty() {}

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        SearchLayout searchLayout = findViewById(R.id.search_layout);
        searchLayout.setCallback(this);

        checkAllButton = findViewById(R.id.select_all);
        checkAllButton.setOnClickListener(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
    }

    public void setItems(List<SelectItem<T>> items) {
        this.items.clear();
        this.items.addAll(items);

        for (final SelectItem<T> item : items) {
            if (item.httpIcon != null) {
                NetUtils.makeBitmapRequest(item.httpIcon, new NetUtilsClasses.BitmapResult() {
                    @Override
                    public void onBitmapFailure(@NonNull HttpUrl source, Exception e) {
                        Logger.e(this, e.getMessage());
                    }

                    @Override
                    public void onBitmapSuccess(@NonNull HttpUrl source, @NonNull Bitmap bitmap) {
                        item.icon = bitmap;
                        if (adapter != null) {
                            int index = adapter.displayList.indexOf(item);
                            if (index != -1) {
                                adapter.notifyItemChanged(index);
                            }
                        }
                    }
                });
            }
        }

        adapter = new SelectAdapter();
        recyclerView.setAdapter(adapter);
        adapter.load();

        updateAllSelected();
    }

    public void setSelectSingle(boolean selectSingle) {
        this.selectSingle = selectSingle;
        this.checkAllButton.setVisibility(selectSingle ? GONE : VISIBLE);

        if (items != null) {
            for (SelectItem<T> item : items) {
                item.checked = false;
            }
            updateAllSelected();
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public List<SelectItem<T>> getItems() {
        return items;
    }

    @Override
    public void onClick(View v) {
        if (v == checkAllButton) {
            for (SelectItem<T> item : items) {
                item.checked = !allChecked;
            }

            updateAllSelected();
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public boolean areAllChecked() {
        return allChecked;
    }

    private void updateAllSelected() {
        int checkedCount = 0;
        for (SelectItem<T> item : items) {
            if (item.checked) {
                checkedCount++;
            }
        }

        allChecked = checkedCount == items.size();
        checkAllButton.setText(allChecked ? R.string.select_none : R.string.select_all);
    }

    private class SelectAdapter
            extends RecyclerView.Adapter<BoardSelectViewHolder> {
        private final List<SelectItem<T>> sourceList = new ArrayList<>();
        private final List<SelectItem<T>> displayList = new ArrayList<>();
        private String searchQuery;

        public SelectAdapter() {
            setHasStableIds(true);
        }

        @Override
        public BoardSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BoardSelectViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cell_select, parent, false));
        }

        @Override
        public void onBindViewHolder(BoardSelectViewHolder holder, int position) {
            SelectItem<T> item = displayList.get(position);
            holder.checkBox.setChecked(item.checked);
            holder.setRadioCheck(item.checked);

            if (selectSingle) {
                holder.checkBox.setVisibility(GONE);
                holder.radioGroup.setVisibility(VISIBLE);
            } else {
                holder.checkBox.setVisibility(VISIBLE);
                holder.radioGroup.setVisibility(GONE);
            }

            if (item.icon != null) {
                holder.icon.setVisibility(VISIBLE);
                holder.icon.setImageBitmap(item.icon);
            } else {
                holder.icon.setVisibility(GONE);
            }

            //noinspection StringEquality this is meant to be a reference comparison, not a string comparison
            if (item.searchTerm == item.name) {
                holder.text.setText(applySearchSpans(item.name, searchQuery));
            } else {
                holder.text.setText(item.name);
            }

            if (item.description != null) {
                holder.description.setVisibility(VISIBLE);
                //noinspection StringEquality this is meant to be a reference comparison, not a string comparison
                if (item.searchTerm == item.description) {
                    holder.description.setText(applySearchSpans(item.description, searchQuery));
                } else {
                    holder.description.setText(item.description);
                }
            } else {
                holder.description.setVisibility(GONE);
            }
        }

        @Override
        public int getItemCount() {
            return displayList.size();
        }

        @Override
        public long getItemId(int position) {
            return displayList.get(position).id;
        }

        public void search(String query) {
            this.searchQuery = query;
            filter();
        }

        private void load() {
            sourceList.clear();
            sourceList.addAll(items);

            filter();
        }

        private void filter() {
            displayList.clear();
            if (!TextUtils.isEmpty(searchQuery)) {
                String query = searchQuery.toLowerCase(Locale.ENGLISH);
                for (SelectItem<T> item : sourceList) {
                    if (item.searchTerm.toLowerCase(Locale.ENGLISH).contains(query)) {
                        displayList.add(item);
                    }
                }
            } else {
                displayList.addAll(sourceList);
            }

            notifyDataSetChanged();
        }
    }

    private class BoardSelectViewHolder
            extends RecyclerView.ViewHolder
            implements CompoundButton.OnCheckedChangeListener, OnClickListener {
        private final CheckBox checkBox;
        private final RadioButton radioButton;
        private final RadioGroup radioGroup;
        private final TextView text;
        private final TextView description;
        private final ImageView icon;

        public BoardSelectViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            radioButton = itemView.findViewById(R.id.radiobutton);
            radioGroup = itemView.findViewById(R.id.radiogroup);
            text = itemView.findViewById(R.id.text);
            description = itemView.findViewById(R.id.description);
            icon = itemView.findViewById(R.id.icon);

            checkBox.setOnCheckedChangeListener(this);
            radioButton.setOnCheckedChangeListener(this);
            itemView.setOnClickListener(this);
        }

        public void setRadioCheck(boolean checked) {
            if (checked) {
                radioButton.setChecked(checked);
            } else {
                radioGroup.clearCheck();
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == checkBox || buttonView == radioButton) {
                SelectItem<T> ourItem = adapter.displayList.get(getAdapterPosition());
                ourItem.checked = isChecked;

                if (selectSingle && buttonView == radioButton && isChecked) {
                    // Deselect every other option
                    int i = 0;
                    for (SelectItem<T> item : adapter.displayList) {
                        if (item != ourItem && item.checked) {
                            item.checked = false;
                            adapter.notifyItemChanged(i);
                        }
                        i++;
                    }
                    for (SelectItem<T> item : adapter.sourceList) {
                        if (item != ourItem) {
                            item.checked = false;
                        }
                    }
                }

                updateAllSelected();
            }
        }

        @Override
        public void onClick(View v) {
            if (selectSingle) {
                radioButton.toggle();
            } else {
                setRadioCheck(!checkBox.isChecked());
            }
        }
    }

    public static class SelectItem<T> {
        public final T item;
        public final long id;
        public final String name;
        public final String description;
        public final String searchTerm;
        public final HttpUrl httpIcon;
        public boolean checked;
        public Bitmap icon;

        public SelectItem(T item, long id, String name, String description, String searchTerm, boolean checked) {
            this(item, id, name, description, searchTerm, checked, null);
        }

        public SelectItem(T item, long id, String name, String description, String searchTerm, boolean checked, HttpUrl httpIcon) {
            this.item = item;
            this.id = id;
            this.name = name;
            this.description = description;
            this.searchTerm = searchTerm;
            this.checked = checked;
            this.httpIcon = httpIcon;
        }
    }
}

