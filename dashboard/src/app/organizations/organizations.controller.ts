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
import {CodenvyTeamEventsManager} from '../../components/api/codenvy-team-events-manager.factory';
import {CodenvyOrganization} from '../../components/api/codenvy-organizations.factory';
import {CodenvyPermissions} from '../../components/api/codenvy-permissions.factory';
import {CodenvyResourcesDistribution} from '../../components/api/codenvy-resources-distribution.factory';

/**
 * @ngdoc controller
 * @name organizations.controller:OrganizationsController
 * @description This class is handling the controller for organizations
 * @author Oleksii Orel
 */
export class OrganizationsController {
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Permissions service.
   */
  private codenvyPermissions: CodenvyPermissions;
  /**
   * Resources distribution service.
   */
  private codenvyResourcesDistribution: CodenvyResourcesDistribution;
  /**
   * Organization API interaction.
   */
  private codenvyOrganization: CodenvyOrganization;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Loading state of the page.
   */
  private isInfoLoading: boolean;
  /**
   * List of root Organizations.
   */
  private rootOrganizations: Array<any> = [];


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyOrganization: CodenvyOrganization, cheNotification: any,
              codenvyTeamEventsManager: CodenvyTeamEventsManager, $scope: ng.IScope,
              $q: ng.IQService, codenvyPermissions: CodenvyPermissions, codenvyResourcesDistribution: CodenvyResourcesDistribution) {
    this.codenvyOrganization = codenvyOrganization;
    this.cheNotification = cheNotification;
    this.$q = $q;
    this.codenvyPermissions = codenvyPermissions;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;

    let refreshHandler = () => {
      this.fetchOrganizations();
    };
    codenvyTeamEventsManager.addDeleteHandler(refreshHandler);
    codenvyTeamEventsManager.addRenameHandler(refreshHandler);

    $scope.$on('$destroy', () => {
      codenvyTeamEventsManager.removeRenameHandler(refreshHandler);
      codenvyTeamEventsManager.removeDeleteHandler(refreshHandler);
    });
    this._updateRootOrganizations(this.codenvyOrganization.getOrganizations());
  }

  /**
   * Fetches the list of root organizations.
   */
  fetchOrganizations(): void {
    this.isInfoLoading = true;
    this.codenvyOrganization.fetchOrganizations().then(() => {
      this.isInfoLoading = false;
      this._updateRootOrganizations(this.codenvyOrganization.getOrganizations());
    }, (error: any) => {
      this.isInfoLoading = false;
      let message = error.data && error.data.message ? error.data.message : 'Failed to retrieve organizations.';
      this.cheNotification.showError(message);
    });
  }

  _updateRootOrganizations(organizations: Array<codenvy.IOrganization>): void {
    this.rootOrganizations = organizations.filter((organization: codenvy.IOrganization) => {
      return !organization.parent;
    });
  }

  /**
   * Gets the list of root organizations.
   *
   * @returns {Array<any>}
   */
  getRootOrganizations(): Array<any> {
    return this.rootOrganizations;
  }
}
