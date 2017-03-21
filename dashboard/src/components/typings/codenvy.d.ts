/*
 *  [2015] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
import codenvy = _codenvy;

declare namespace _codenvy {

  export interface ILink {
    href: string;
    method: string;
    parameters: Array<any>;
    produces: string;
    rel: string;
  }

  export interface IOrganization {
    id: string;
    links: Array<ILink>;
    name: string;
    parent?: string;
    qualifiedName: string;
  }

  export interface IPermissions {
    actions: Array<string>;
    domainId: string;
    instanceId: string;
    userId: string;
  }

  export interface IRole {
    actions: Array<string>;
    description: string;
    title: string;
  }

  export interface IMember extends IUser {
    id: string;
    permissions?: IPermissions;
    roles?: Array<IRole>;
  }

  export interface IUser {
    attributes: {
      firstName?: string;
      lastName?: string;
      [propName: string]: string | number;
    };
    email: string;
    links: Array<ILink>;
    name: string;
    userId: string;
  }
}
