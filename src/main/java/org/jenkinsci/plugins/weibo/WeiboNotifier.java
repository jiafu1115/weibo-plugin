package org.jenkinsci.plugins.weibo;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.jenkinsci.plugins.weibo.client.WeiboClient;
import org.jenkinsci.plugins.weibo.token.WeiboToken;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link WeiboNotifier} is created. The created instance is persisted to the
 * project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #name}) to remember the configuration.
 *
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
 * invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class WeiboNotifier extends Notifier {

    public static final String DEFAULT_CONTENT_KEY = "${DEFAULT_CONTENT}";
    public static final String DEFAULT_CONTENT_VALUE = "${BUILD_STATUS}  ${JOB_NAME}:${BUILD_NUMBER}  ${JOB_URL}";

    private final boolean disable;
    private final String weiboAccountId;
    private final String publishContent;

    // Fields in config.jelly must match the parameter names in the
    // "DataBoundConstructor"
    @DataBoundConstructor
    public WeiboNotifier(boolean disable, String weiboAccountId, String publishContent) {
        this.disable = disable;
        this.weiboAccountId = weiboAccountId;
        this.publishContent = publishContent;
        System.out.println("save:");
        System.out.println(disable);
        System.out.println(weiboAccountId);
        System.out.println(publishContent);
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getPublishContent() {
        return publishContent;
    }

    public String getWeiboAccountId() {
        return weiboAccountId;
    }

    public boolean isDisable() {
        return disable;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a
        // build.

        // This also shows how you can consult the global configuration of the
        // builder

        PrintStream logger = listener.getLogger();
        if (!disable) {
            logger.println("[Weibo Plugin]================[start]=================");
            try {
                DescriptorImpl descriptor = getDescriptor();
                WeiboAccount weiboAccount = descriptor.getWeiboAccount(weiboAccountId);

                logger.println("[Weibo Plugin][Expand content]Before Expand: " + publishContent);
                String publishContentAfterInitialExpand=publishContent;
                if(publishContent.contains(DEFAULT_CONTENT_KEY)){
                    publishContentAfterInitialExpand=publishContent.replace(DEFAULT_CONTENT_KEY, DEFAULT_CONTENT_VALUE);
                }
                String expandAll = TokenMacro.expandAll(build, listener, publishContentAfterInitialExpand, false, getPrivateMacros());
                logger.println("[Weibo Plugin][Expand content]After Expand: " + expandAll);

                logger.println("[Weibo Plugin][Publish Content][begin]use:" + weiboAccount);
                WeiboClient.sent(weiboAccount, expandAll);
                logger.println("[Weibo Plugin][Publish Content][end]");

                logger.println("[Weibo Plugin]================[end][success]=================");
            } catch (Exception e) {
                logger.println("[Weibo Plugin]" + e.getMessage());
                logger.println("[Weibo Plugin]" + Arrays.toString(e.getStackTrace()));
                logger.println("[Weibo Plugin]================[end][failure]=================");
            }

        } else {
            logger.println("[Weibo Plugin]================[skiped]=================");

        }

        return true;
    }

    private static List<TokenMacro> getPrivateMacros() {
        List<TokenMacro> macros = new ArrayList<TokenMacro>();
        ClassLoader cl = Jenkins.getInstance().pluginManager.uberClassLoader;
        for (final IndexItem<WeiboToken, TokenMacro> item : Index.load(WeiboToken.class, TokenMacro.class, cl)) {
            try {
                macros.add(item.instance());
            } catch (Exception e) {
                // ignore errors loading tokens
            }
        }
        return macros;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    /**
     * Descriptor for {@link WeiboNotifier}. Used as a singleton. The class is
     * marked as public so that it can be accessed from views.
     *
     * <p>
     * See
     * <tt>src/main/resources/hudson/plugins/hello_world/WeiboNotifier/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    // This indicates to Jenkins that this is an implementation of an extension
    // point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information, simply store it in a
         * field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private final CopyOnWriteList<WeiboAccount> weiboAccounts = new CopyOnWriteList<WeiboAccount>();

        public DescriptorImpl() {
            super(WeiboNotifier.class);
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *            This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the
         *         browser.
         */
        /*
         * public FormValidation doCheckName(@QueryParameter String value)
         * throws IOException, ServletException { if (value.length() == 0)
         * return FormValidation.error("Please set a name"); if (value.length()
         * < 4) return FormValidation.warning("Isn't the name too short?");
         * return FormValidation.ok(); }
         */

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public WeiboAccount[] getWeiboAccounts() {
            return weiboAccounts.toArray(new WeiboAccount[weiboAccounts.size()]);
        }

        public WeiboAccount getWeiboAccount(String weiboAccountId) {
            for (WeiboAccount weiboAccount : weiboAccounts) {
                if (weiboAccount.getId().equalsIgnoreCase(weiboAccountId))
                    return weiboAccount;
            }

            throw new RuntimeException("no such key: " + weiboAccountId);
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Weibo Notification";
        }

        public FormValidation doIdCheck(@QueryParameter String id) throws IOException, ServletException {
             FormValidation basicVerify = returnVerify(id,"id");
            if(basicVerify.kind.equals(FormValidation.ok().kind)){
                 int total=0;
                 for (WeiboAccount weiboAccount : weiboAccounts) {
                     if(weiboAccount.getId().equalsIgnoreCase(id.trim())){
                         total++;
                     }
                 }
                 if(total>1){
                     return  FormValidation.error("duplicated id: "+id);
                 }
                 return FormValidation.ok();
             }else{
               return basicVerify;
            }
         }

        public FormValidation doPasswordCheck(@QueryParameter String password) throws IOException, ServletException {
            return returnVerify(password,"password");
         }

        public FormValidation doUsernameCheck(@QueryParameter String username) throws IOException, ServletException {
            return returnVerify(username,"username");
        }

        private FormValidation returnVerify(String value, String message) {
            if (null == value||value.length() == 0)
                return FormValidation.error("please input "+message);

            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            weiboAccounts.replaceBy(req.bindParametersToList(WeiboAccount.class, "weibo.account."));

            for (WeiboAccount weiboAccount : weiboAccounts) {
                System.out.println(weiboAccount);
            }
            save();
            return true;
        }

    }

}
