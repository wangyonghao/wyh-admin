import { $t } from '@vben/locales';

import { z } from 'zod';

/**
 * Login form validation schema
 * Defines validation rules for username, password, and captcha fields
 */
export const loginFormSchema = z.object({
  /**
   * Login username is non-empty only.
   * Account creation requires 4-64; existing shorter names stay valid here.
   */
  username: z.string().min(1, { message: $t('authentication.usernameTip') }),

  /**
   * Password field validation
   * Required field with minimum length of 1 character
   */
  password: z.string().min(1, { message: $t('authentication.passwordTip') }),

  /**
   * Captcha field validation
   * Optional field that becomes required when captcha is enabled
   * Uses conditional validation based on captcha availability
   */
  captcha: z
    .string()
    .min(1, { message: $t('authentication.verifyRequiredTip') })
    .optional(),

  /**
   * UUID field for captcha verification
   * Hidden field that stores the captcha session identifier
   */
  uuid: z.string().optional(),
});

/**
 * Type definition for login form data
 * Inferred from the validation schema
 */
export type LoginFormData = z.infer<typeof loginFormSchema>;

/**
 * Default values for the login form
 * Used as initial values when creating the form
 */
export const loginFormDefaults: LoginFormData = {
  username: '',
  password: '',
  captcha: '',
  uuid: '',
};

/**
 * Conditional validation schema for when captcha is enabled
 * Makes captcha field required when captcha is active
 */
export const createLoginFormSchemaWithCaptcha = (captchaEnabled: boolean) => {
  if (captchaEnabled) {
    return loginFormSchema.extend({
      captcha: z
        .string()
        .min(1, { message: $t('authentication.verifyRequiredTip') }),
    });
  }
  return loginFormSchema;
};
