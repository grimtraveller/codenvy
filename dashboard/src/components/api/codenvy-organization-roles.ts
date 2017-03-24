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
'use strict';

/**
 * This is enum of organization roles.
 *
 * @author Oleksii Orel
 */
export enum CodenvyOrganizationRoles {
  MEMBER = <any> {
    'title': 'Developer',
    'description': 'Can create and use own workspaces.',
    'actions' : ['createWorkspaces']},
  ADMIN = <any> {
    'title': 'Admin',
    'description': 'Can edit the organization’s settings, manage workspaces and members.',
    'actions' : ['update', 'setPermissions', 'manageResources', 'manageWorkspaces', 'createWorkspaces', 'delete', 'manageSuborganizations']
  }
}

export namespace CodenvyOrganizationRoles {
  export function getValues(): Array<any> {
    return [CodenvyOrganizationRoles.MEMBER, CodenvyOrganizationRoles.ADMIN];
  }
}
