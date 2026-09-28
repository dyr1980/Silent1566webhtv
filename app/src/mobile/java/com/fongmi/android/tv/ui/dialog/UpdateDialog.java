package com.fongmi.android.tv.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Update;
import com.fongmi.android.tv.databinding.DialogUpdateBinding;
import com.fongmi.android.tv.impl.UpdateListener;
import com.fongmi.android.tv.utils.AppVersion;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.MarkdownText;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Util;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UpdateDialog extends BaseAlertDialog {

    private DialogUpdateBinding binding;
    private UpdateListener listener;
    private Update stable;
    private Update beta; // [Beta 恢复点]
    private String selected = Update.CHANNEL_STABLE; // [Beta 恢复点]
    private boolean stableExpanded = true; // [Beta 恢复点]
    private boolean betaExpanded; // [Beta 恢复点]
    private boolean downloading;

    public static UpdateDialog create() {
        return new UpdateDialog();
    }

    public UpdateDialog stable(Update stable) {
        this.stable = stable;
        return this;
    }

    public UpdateDialog beta(Update beta) {
        // this.beta = beta; // [Beta 恢复点]
        return this;
    }

    public UpdateDialog selected(String selected) {
        // [Beta 恢复点] 暂时注释掉，以后需要 Beta 时取消下方注释
        /*
        this.selected = selected;
        this.stableExpanded = !Update.CHANNEL_BETA.equals(selected);
        this.betaExpanded = Update.CHANNEL_BETA.equals(selected);
        */
        return this;
    }

    public UpdateDialog listener(UpdateListener listener) {
        this.listener = listener;
        return this;
    }

    public UpdateDialog show(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), null);
        return this;
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogUpdateBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return new MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_WebHTV_LightDialog).setView(getBinding().getRoot()).setCancelable(false);
    }

    @Override
    protected void initView() {
        binding.progress.setMax(100);
        render();
    }

    @Override
    protected void initEvent() {
        binding.close.setOnClickListener(this::close);
        binding.stableItem.setOnClickListener(view -> toggle(Update.CHANNEL_STABLE));
        // binding.betaItem.setOnClickListener(view -> toggle(Update.CHANNEL_BETA)); // [Beta 恢复点]
        binding.stableConfirm.setOnClickListener(view -> update(Update.CHANNEL_STABLE, view));
        // binding.betaConfirm.setOnClickListener(view -> update(Update.CHANNEL_BETA, view)); // [Beta 恢复点]
        binding.cancel.setOnClickListener(this::action);
    }

    @Override
    public void onStart() {
        super.onStart();
        setCancelable(false);
        if (getDialog() != null) getDialog().setCanceledOnTouchOutside(false);
        setDialogWidth(ResUtil.isLand(requireActivity()) ? 0.62f : 0.92f);
    }

    private void select(String channel) {
        selected = channel;
        if (listener != null) listener.onChannel(channel);
    }

    private void toggle(String channel) {
        if (isExpanded(channel)) {
            update(channel, getItem(channel));
            return;
        }
        // [Beta 恢复点] 暂时注释掉 Beta 的展开逻辑
        /*
        if (!hasBeta()) return;
        selected = channel;
        stableExpanded = Update.CHANNEL_STABLE.equals(channel);
        betaExpanded = Update.CHANNEL_BETA.equals(channel);
        if (listener != null) listener.onChannel(channel);
        render();
        */
    }

    private void action(View view) {
        if (downloading) {
            if (listener != null) listener.onCancel(view);
            return;
        }
        Update update = getSelected();
        if (update != null && update.hasUpdate()) update(selected, view);
        else if (listener != null) listener.onCancel(view);
    }

    private void close(View view) {
        if (downloading) return;
        if (listener != null) listener.onClose();
        dismissAllowingStateLoss();
    }

    private void update(String channel, View view) {
        select(channel);
        if (listener != null) listener.onConfirm(view);
    }

    private void render() {
        normalizeSelection();
        // binding.betaItem.setVisibility(hasBeta() ? View.VISIBLE : View.GONE); // [Beta 恢复点]
        renderItem(Update.CHANNEL_STABLE, stable);
        // if (hasBeta()) renderItem(Update.CHANNEL_BETA, beta); // [Beta 恢复点]
        renderAction();
        binding.close.setVisibility(View.VISIBLE);
        binding.progressPanel.setVisibility(View.GONE);
        downloading = false;
    }

    private void renderItem(String channel, Update update) {
        boolean stableChannel = Update.CHANNEL_STABLE.equals(channel);
        boolean expanded = stableChannel ? stableExpanded : betaExpanded;
        View item = stableChannel ? binding.stableItem : binding.betaItem;
        View content = stableChannel ? binding.stableContent : binding.betaContent;
        item.setSelected(expanded);
        content.setVisibility(expanded ? View.VISIBLE : View.GONE);

        if (stableChannel) {
            binding.stableVersion.setText(getVersion(update));
            binding.stableStatus.setText(getStatus(update));
            binding.stableExpand.setVisibility(hasBeta() && !expanded ? View.VISIBLE : View.GONE);
            binding.stableExpand.setText(R.string.update_expand);
            binding.stableDesc.setText(MarkdownText.render(getBody(update), getString(R.string.update_no_notes)));

            if (update != null && update.hasUpdate()) {
                binding.stableConfirm.setVisibility(View.VISIBLE);
                binding.stableConfirm.setEnabled(true);
                binding.stableConfirm.setText(R.string.update_confirm);
            } else {
                binding.stableConfirm.setVisibility(View.GONE);
            }
        } else {
            // [Beta 恢复点] 暂时注释掉 Beta 卡片的渲染逻辑
            /*
            binding.betaVersion.setText(getVersion(update));
            binding.betaStatus.setText(getStatus(update));
            binding.betaExpand.setVisibility(!expanded ? View.VISIBLE : View.GONE);
            binding.betaExpand.setText(R.string.update_expand);
            binding.betaDesc.setText(MarkdownText.render(getBody(update), getString(R.string.update_no_notes)));

            if (update != null && update.hasUpdate()) {
                binding.betaConfirm.setVisibility(View.VISIBLE);
                binding.betaConfirm.setEnabled(true);
                binding.betaConfirm.setText(R.string.update_confirm);
            } else {
                binding.betaConfirm.setVisibility(View.GONE);
            }
            */
        }
    }

    private void renderAction() {
        Update update = getSelected();
        if (update == null || !update.hasUpdate()) binding.cancel.setText(R.string.about_acknowledge);
        else binding.cancel.setText(hasBeta() ? getString(R.string.update_confirm_channel, getSelectedName()) : getString(R.string.update_confirm));
    }

    private String getVersion(Update update) {
        return update != null && update.hasManifest() ? AppVersion.stripPrefix(update.name) : getString(R.string.update_status_unavailable);
    }

    private String getStatus(Update update) {
        if (update == null || !update.hasManifest()) return getString(R.string.update_status_unavailable);
        return update.hasUpdate() ? getString(R.string.update_status_available) : getString(R.string.update_status_latest);
    }

    private String getBody(Update update) {
        if (update == null || !update.hasManifest()) return getString(R.string.update_channel_unavailable);
        if (!TextUtils.isEmpty(update.getText())) return update.getText();
        if (!update.hasUpdate()) return getString(R.string.update_channel_latest);
        return update.getText();
    }

    // [Beta 恢复点] 目前强制返回 false，以后恢复时改为：return beta != null && beta.hasManifest();
    private boolean hasBeta() {
        return false;
    }

    private void normalizeSelection() {
        // [Beta 恢复点] 暂时注释掉
        /*
        if (hasBeta()) return;
        selected = Update.CHANNEL_STABLE;
        stableExpanded = true;
        betaExpanded = false;
        */
        selected = Update.CHANNEL_STABLE;
        stableExpanded = true;
        betaExpanded = false;
    }

    private Update getSelected() {
        // [Beta 恢复点] 暂时只返回 stable
        // return Update.CHANNEL_BETA.equals(selected) ? beta : stable;
        return stable;
    }

    private boolean isExpanded(String channel) {
        // [Beta 恢复点] 暂时只判断 stable
        // return Update.CHANNEL_BETA.equals(channel) ? betaExpanded : stableExpanded;
        return stableExpanded;
    }

    private View getItem(String channel) {
        // [Beta 恢复点] 暂时只返回 stableItem
        // return Update.CHANNEL_BETA.equals(channel) ? binding.betaItem : binding.stableItem;
        return binding.stableItem;
    }

    private String getSelectedName() {
        // [Beta 恢复点] 暂时只返回 Stable 名称
        // return getString(Update.CHANNEL_BETA.equals(selected) ? R.string.update_channel_beta : R.string.update_channel_stable);
        return getString(R.string.update_channel_stable);
    }

    private void setDialogWidth(float factor) {
        Window window = getDialog() == null ? null : getDialog().getWindow();
        if (window == null) return;
        int width = (int) (ResUtil.getScreenWidth(requireActivity()) * factor);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = width;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(params);
        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public boolean setProgress(int progress) {
        return setProgress(progress, 0, 0, 0, 0);
    }

    public boolean setProgress(int progress, long bytes, long total, long speed, long elapsed) {
        if (!isProgressTarget()) return false;
        downloading = true;
        boolean indeterminate = progress < 0;
        int value = Math.max(0, Math.min(100, progress));

        binding.stableItem.setEnabled(false);
        // binding.betaItem.setEnabled(false); // [Beta 恢复点]
        binding.stableConfirm.setEnabled(false);
        // binding.betaConfirm.setEnabled(false); // [Beta 恢复点]

        binding.cancel.setEnabled(true);
        binding.close.setVisibility(View.GONE);
        binding.progressPanel.setVisibility(View.VISIBLE);
        binding.progress.setIndeterminate(indeterminate);
        if (!indeterminate) binding.progress.setProgress(value);
        binding.progressText.setText(getProgressText(indeterminate, value, bytes, total, speed, elapsed));
        binding.cancel.setText(R.string.update_cancel);
        return true;
    }

    private boolean isProgressTarget() {
        return binding != null && isAdded() && getContext() != null
                && getDialog() != null && getDialog().isShowing();
    }

    private String getProgressText(boolean indeterminate, int value, long bytes, long total, long speed, long elapsed) {
        if (speed <= 0 || elapsed <= 0) return indeterminate ? getString(R.string.update_downloading_unknown) : getString(R.string.update_downloading, value);
        String speedText = FileUtil.byteCountToDisplaySize(speed);
        String elapsedText = formatDuration(elapsed);
        if (!indeterminate && total > 0 && bytes >= 0) {
            long remaining = Math.max(0, total - bytes) * 1000 / speed;
            return getString(R.string.update_downloading_detail_remaining, value, speedText, formatDuration(remaining), elapsedText);
        }
        return indeterminate ? getString(R.string.update_downloading_detail_unknown, speedText, elapsedText) : getString(R.string.update_downloading_detail, value, speedText, elapsedText);
    }

    private String formatDuration(long time) {
        String text = Util.timeMs(Math.max(0, time));
        return TextUtils.isEmpty(text) ? "00:00" : text;
    }
}
