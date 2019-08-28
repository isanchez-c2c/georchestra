/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.ws.passwordrecovery;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.UserTokenDao;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 * This controller implements the interactions required to ask for a new
 * password based on a token provide.
 * 
 * @author Mauricio Pazos
 *
 */
@Controller
@SessionAttributes(types = NewPasswordFormBean.class)
public class NewPasswordFormController {

    private static final Log LOG = LogFactory.getLog(NewPasswordFormController.class.getName());

    private AccountDao accountDao;
    private UserTokenDao userTokenDao;

    @Autowired
    public NewPasswordFormController(AccountDao accountDao, UserTokenDao userTokenDao) {
        this.accountDao = accountDao;
        this.userTokenDao = userTokenDao;
    }

    @InitBinder
    public void initForm(WebDataBinder dataBinder) {

        dataBinder.setAllowedFields(new String[] { "password", "confirmPassword" });
    }

    /**
     * Search the user associated to the provided token, then initialize the
     * {@link NewPasswordFormBean}. If the token is not valid (it didn't exist in
     * the system registry) the PasswordRecoveryForm is presented to offer a new
     * chance to the user.
     * 
     * @param token the token was generated by the
     *              {@link PasswordRecoveryFormController}}
     * @param model
     * 
     * @return newPasswordForm or passwordRecoveryForm
     * 
     * @throws IOException
     */
    @RequestMapping(value = "/account/newPassword", method = RequestMethod.GET)
    public String setupForm(@RequestParam("token") String token, Model model) throws IOException {

        try {
            final String uid = this.userTokenDao.findUserByToken(token);

            NewPasswordFormBean formBean = new NewPasswordFormBean();

            formBean.setToken(token);
            formBean.setUid(uid);

            model.addAttribute(formBean);

            return "newPasswordForm";

        } catch (NameNotFoundException e) {

            return "passwordRecoveryForm";

        } catch (DataServiceException e) {

            LOG.error("cannot insert the setup the passwordRecoveryForm. " + e.getMessage());

            throw new IOException(e);
        }

    }

    /**
     * Registers the new password, if it is valid.
     * 
     * @param formBean
     * @param result
     * @param sessionStatus
     * 
     * @return the next view
     * 
     * @throws IOException
     */
    @RequestMapping(value = "/account/newPassword", method = RequestMethod.POST)
    public String newPassword(@ModelAttribute NewPasswordFormBean formBean, BindingResult result,
            SessionStatus sessionStatus) throws IOException {

        PasswordUtils.validate(formBean.getPassword(), formBean.getConfirmPassword(), result);

        if (result.hasErrors()) {

            return "newPasswordForm";
        }

        // changes the user's password and removes the token
        try {

            String uid = formBean.getUid();
            String password = formBean.getPassword();

            this.accountDao.changePassword(uid, password);

            this.userTokenDao.delete(uid);

            sessionStatus.setComplete();

            return "passwordUpdated";

        } catch (DataServiceException e) {
            LOG.error("cannot set the the new password. " + e.getMessage());

            throw new IOException(e);

        }
    }

    @ModelAttribute("newPasswordFormBean")
    public NewPasswordFormBean getNewPasswordFormBean() {
        return new NewPasswordFormBean();
    }
}
