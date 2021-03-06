/**
 *
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 1.0.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

import * as models from './models';

export interface LoginBusinessProperties {
    websiteId?: number;

    usernameNeeded?: boolean;

    usernameGeneratedOrChosen?: boolean;

    generationServiceUrl?: string;

    generationServiceUrlPublicKey?: string;

    generationServiceUrlPrivateKey?: string;

    addressNeeded?: boolean;

    enableVAT?: boolean;

    enableVATValidation?: boolean;

    numberOfShopsNeeded?: boolean;

    numberOfShopsNeededPerCountry?: boolean;

    priceCountryDependent?: boolean;

    revenuesNeeded?: boolean;

    form?: Array<models.FormInput>;

}
