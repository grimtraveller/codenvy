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
import {CodenvyOrganization} from '../../../../components/api/codenvy-organizations.factory';

/**
 * @ngdoc controller
 * @name organizations.list.Item.controller:OrganizationsItemController
 * @description This class is handling the controller for item of organizations list
 * @author Oleksii Orel
 */
export class OrganizationsItemController {
  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Organization API interaction.
   */
  private codenvyOrganization: CodenvyOrganization;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Organization details (the value is set in directive attributes).
   */
  private organization: codenvy.IOrganization;
  /**
   * Callback needed to react on organizations updation (the value is set in directive attributes).
   */
  private onUpdate: Function;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, codenvyOrganization: CodenvyOrganization, confirmDialogService: any, cheNotification: any) {
    this.$location = $location;
    this.confirmDialogService = confirmDialogService;
    this.codenvyOrganization = codenvyOrganization;
    this.cheNotification = cheNotification;
  }

  /**
   * Redirect to factory details.
   */
  redirectToOrganizationDetails(page: string) {
    let path = '/organization/' + this.organization.qualifiedName;
    if (page) {
      this.$location.path(path).search({page: page});
    } else {
      this.$location.path(path);
    }
  }

  /**
   * Removes organization after confirmation.
   */
  removeOrganization(): void {
    this.confirmRemoval().then(() => {
      this.codenvyOrganization.deleteOrganization(this.organization.id).then(() => {
        this.onUpdate();
      }, (error: any) => {
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete organization ' + this.organization.name);
      });
    });
  }

  /**
   * Shows dialog to confirm the current organization removal.
   *
   * @returns {angular.IPromise<any>}
   */
  confirmRemoval(): ng.IPromise<any> {
    let promise = this.confirmDialogService.showConfirmDialog('Delete organization',
      'Would you like to delete organization \'' + this.organization.name + '\'?', 'Delete');
    return promise;
  }
}
