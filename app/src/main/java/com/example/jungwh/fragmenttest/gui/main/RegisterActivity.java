package com.example.jungwh.fragmenttest.gui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.jungwh.fragmenttest.R;
import com.example.jungwh.fragmenttest.business.logic.RegisterService;
import com.example.jungwh.fragmenttest.util.ExceptionHelper;
import com.example.jungwh.fragmenttest.util.KeyboardHelper;
import com.example.jungwh.fragmenttest.util.ShowProgressHelper;

import org.json.JSONException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jungwh on 2016-10-04.
 */

public class RegisterActivity extends AppCompatActivity {
    private static final Pattern VALID_PASSWOLD_REGEX_ALPHA_NUM = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{5,16}$"); // 5자리 ~ 16자리까지 가능
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private EditText etUserId, etUserPassword, etUserNm, etMoblphon, etEmail;
    private View viewProgress, viewRegisterForm;
    private RegisterTask authTask = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTitle("회원가입");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etUserId = (EditText) findViewById(R.id.user_id);
        etUserPassword = (EditText) findViewById(R.id.user_password);
        etUserNm = (EditText) findViewById(R.id.user_nm);
        etMoblphon = (EditText) findViewById(R.id.moblphon);
        etEmail = (EditText) findViewById(R.id.email);

        viewRegisterForm = findViewById(R.id.activity_register_form);
        viewProgress = findViewById(R.id.activity_register_layout);

        (findViewById(R.id.register)).setOnClickListener(mOnClickListener);
    }

    Button.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveData();
        }
    };

    public void saveData(){
        if (authTask != null) {
            return;
        }

        KeyboardHelper.hideSoftKeyboard(this);

        // Reset errors.
        etUserId.setError(null);
        etUserPassword.setError(null);

        String userId = etUserId.getText().toString();
        String userPassword = etUserPassword.getText().toString();
        String userNm = etUserNm.getText().toString();
        String moblphon = etMoblphon.getText().toString();
        String email = etEmail.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(userId)) {
            etUserId.setError("사용자 ID를 입력해 주십시오");
            focusView = etUserId;
            cancel = true;
        }else if (TextUtils.isEmpty(userPassword)) {
            etUserPassword.setError("비밀번호를 입력해 주십시오");
            focusView = etUserPassword;
            cancel = true;
        }else if (!isPasswordValid(userPassword)) {
            etUserPassword.setError("비밀번호는 4자리 보다 더 길어야 합니다");
            focusView = etUserPassword;
            cancel = true;
        }else if (!validatePassword(userPassword)) {
            etUserPassword.setError("비밀번호는 한글을 포함 할 수 없습니다");
            focusView = etUserPassword;
            cancel = true;
        }else if (TextUtils.isEmpty(userNm)) {
            etUserNm.setError("닉네임을 입력해 주십시오");
            focusView = etUserNm;
            cancel = true;
        }else if (TextUtils.isEmpty(moblphon)) {
            etMoblphon.setError("핸드폰번호를 입력해 주십시오");
            focusView = etMoblphon;
            cancel = true;
        }else if (TextUtils.isEmpty(email)) {
            etEmail.setError("이메일을 입력해 주십시오");
            focusView = etEmail;
            cancel = true;
        }else if (!validateEmail(email)) {
            etEmail.setError("이메일 형식이 잘못되었습니다");
            focusView = etEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            authTask = new RegisterTask(getApplicationContext() , userId, userPassword, userNm, moblphon, email);
            authTask.execute((Void) null);
        }
    }

    private boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static boolean validatePassword(String pwStr) {
        Matcher matcher = VALID_PASSWOLD_REGEX_ALPHA_NUM.matcher(pwStr);
        return matcher.matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            setResult(RESULT_OK, null);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            setResult(RESULT_OK, null);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RegisterTask
            extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final String userId;
        private final String password;
        private final String userNm;
        private final String moblphon;
        private final String email;
        private RegisterService registerService = new RegisterService();
        private String registerErrMsg;

        RegisterTask(Context context, String userId, String password, String userNm, String moblphon, String email) {
            // 추후에 서버 validation추가해서 response로 errmsg넘겨주고 포커싱이랑, 포커싱에 해당하는 메시지붙이는작업해야함.
            registerErrMsg = "가입에 실패하셨습니다.";
            this.context = context;
            this.userId = userId;
            this.password = password;
            this.userNm = userNm;
            this.moblphon = moblphon;
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ShowProgressHelper.showProgress(context, true, viewProgress, viewRegisterForm);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return registerService.register(userId, password, userNm, moblphon, email);
            } catch (JSONException | IOException e) {
                registerErrMsg = ExceptionHelper.getApplicationExceptionMessage(e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            authTask = null;
            ShowProgressHelper.showProgress(context, false, viewProgress, viewRegisterForm);

            if (success) {
                finish();
            } else {
                etUserPassword.setError(registerErrMsg);
                etUserPassword.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            ShowProgressHelper.showProgress(context, false, viewProgress, viewRegisterForm);
        }
    }
}
