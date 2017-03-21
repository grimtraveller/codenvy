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
import {CodenvyOrganization} from '../../../components/api/codenvy-organizations.factory';
import {CodenvyResourceLimits} from '../../../components/api/codenvy-resource-limits';
import {CodenvyPermissions} from '../../../components/api/codenvy-permissions.factory';
import {CodenvyResourcesDistribution} from '../../../components/api/codenvy-resources-distribution.factory';


/**
 * @ngdoc controller
 * @name organizations.list.controller:ListOrganizationsController
 * @description This class is handling the controller for listing the organizations
 * @author Oleksii Orel
 */
export class ListOrganizationsController {
  /**
   * Organization API interaction.
   */
  private codenvyOrganization: CodenvyOrganization;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
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
   * List of organizations.
   */
  private organizations: Array<any>;
  /**
   * Map of organization members.
   */
  private organizationMembers: Map<string, number>;
  /**
   * Map of organization resources.
   */
  private organizationResources: Map<string, any>;
  /**
   * Selected status of organizations in the list.
   */
  private organizationsSelectedStatus: { [organizationId: string]: boolean; };
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Bulk operation checked state.
   */
  private isBulkChecked: boolean;
  /**
   * No selected workspace state.
   */
  private isNoSelected: boolean;
  /**
   * All selected workspace state.
   */
  private isAllSelected: boolean;
  /**
   * On update function.
   */
  private onUpdate: Function;

  /**
   * Parent organization name.
   */
  private parentName: string;
  /**
   * User order by.
   */
  private userOrderBy: string;
  /**
   * Organization filter.
   */
  private organizationFilter: Object;

  /**
   * Is root organizations
   */
  private isRootOrganizations: boolean;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $scope: ng.IScope, codenvyPermissions: CodenvyPermissions, codenvyResourcesDistribution: CodenvyResourcesDistribution, codenvyOrganization: CodenvyOrganization, cheNotification: any, confirmDialogService: any, $route: ng.route.IRouteService) {
    this.$q = $q;
    this.cheNotification = cheNotification;
    this.codenvyPermissions = codenvyPermissions;
    this.codenvyOrganization = codenvyOrganization;
    this.confirmDialogService = confirmDialogService;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;

    this.parentName = $route.current.params.organizationName;
    this.isNoSelected = true;
    this.userOrderBy = 'name';
    this.organizationFilter = {name: ''};
    this.organizationsSelectedStatus = {};

    $scope.$watch(() => {
      return this.organizations;
    }, () => {
      this.processOrganizations();
    });
    this.processOrganizations();
  }

  /**
   * Process organization - retrieving additional data.
   */
  processOrganizations(): void {
    if (!this.organizations || !this.organizations.length) {
      return;
    }
    this.organizationMembers = new Map();
    this.organizationResources = new Map();

    let promises = [];
    this.isRootOrganizations = true;
    this.organizations.forEach((organization: codenvy.IOrganization) => {
      if (this.isRootOrganizations && organization.parent) {
        this.isRootOrganizations = false;
      }
      let promiseMembers = this.codenvyPermissions.fetchOrganizationPermissions(organization.id).then(() => {
        this.organizationMembers.set(organization.id, this.codenvyPermissions.getOrganizationPermissions(organization.id).length);
      }, (error: any) => {
        if (error.status === 304) {
          this.organizationMembers.set(organization.id, this.codenvyPermissions.getOrganizationPermissions(organization.id).length);
        }
      });
      promises.push(promiseMembers);

      let promiseResource = this.codenvyResourcesDistribution.fetchOrganizationResources(organization.id).then(() => {
        this.processResource(organization.id);
      }, (error: any) => {
        if (error.status === 304) {
          this.processResource(organization.id);
        }
      });

      promises.push(promiseResource);
    });

    this.$q.all(promises).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Process organization resources.
   *
   * @param organizationId organization's id
   */
  processResource(organizationId: string): void {
    let ramLimit = this.codenvyResourcesDistribution.getOrganizationResourceByType(organizationId, CodenvyResourceLimits.RAM);
    this.organizationResources.set(organizationId, ramLimit ? ramLimit.amount : undefined);
  }

  /**
   * Returns the number of organization's members.
   *
   * @param organizationId organization's id
   * @returns {any} number of organization members to display
   */
  getMembersCount(organizationId: string): any {
    if (this.organizationMembers && this.organizationMembers.size > 0) {
      return this.organizationMembers.get(organizationId) || '-';
    }
    return '-';
  }

  /**
   * Returns the RAM limit value.
   *
   * @param organizationId organization's id
   * @returns {any}
   */
  getRamCap(organizationId: string): any {
    if (this.organizationResources && this.organizationResources.size > 0) {
      let ram = this.organizationResources.get(organizationId);
      return ram ? (ram / 1024) : null;
    }
    return null;
  }

  /**
   * return true if all organizations in list are checked
   * @returns {boolean}
   */
  isAllOrganizationsSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * returns true if all organizations in list are not checked
   * @returns {boolean}
   */
  isNoOrganizationsSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Check all organizations in list
   */
  selectAllOrganizations(): void {
    this.organizations.forEach((organization: codenvy.IOrganization) => {
      this.organizationsSelectedStatus[organization.id] = true;
    });
  }

  /**
   * Uncheck all organizations in list
   */
  deselectAllOrganizations(): void {
    Object.keys(this.organizationsSelectedStatus).forEach((key: string) => {
      this.organizationsSelectedStatus[key] = false;
    });
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllOrganizations();
      this.isBulkChecked = false;
    } else {
      this.selectAllOrganizations();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update organization selected status
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    Object.keys(this.organizationsSelectedStatus).forEach((key: string) => {
      if (this.organizationsSelectedStatus[key]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });

    if (this.isNoSelected) {
      this.isBulkChecked = false;
      return;
    }

    if (this.isAllSelected) {
      this.isBulkChecked = true;
    }
  }

  /**
   * Delete all selected organizations.
   */
  deleteSelectedOrganizations(): void {
    let organizationsSelectedStatusKeys = Object.keys(this.organizationsSelectedStatus);
    let checkedOrganizationKeys = [];

    if (!organizationsSelectedStatusKeys.length) {
      this.cheNotification.showError('No such organization.');
      return;
    }

    organizationsSelectedStatusKeys.forEach((key: string) => {
      if (this.organizationsSelectedStatus[key] === true) {
        checkedOrganizationKeys.push(key);
      }
    });

    if (!checkedOrganizationKeys.length) {
      this.cheNotification.showError('No such organization.');
      return;
    }

    let confirmationPromise = this._showDeleteOrganizationConfirmation(checkedOrganizationKeys.length);
    confirmationPromise.then(() => {
      let promises = [];

      checkedOrganizationKeys.forEach((organizationId: string) => {
        this.organizationsSelectedStatus[organizationId] = false;

        let promise = this.codenvyOrganization.deleteOrganization(organizationId).then(() => {
          //
        }, (error: any) => {
          this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete organization ' + organizationId + '.');
        });

        promises.push(promise);
      });

      this.$q.all(promises).finally(() => {
        if (typeof this.onUpdate !== 'undefined') {
          this.onUpdate();
        }
        this.updateSelectedStatus();
      });
    });
  }

  /**
   * Show confirmation popup before organization deletion.
   *
   * @param numberToDelete number of organization to be deleted
   * @returns {ng.IPromise<any>}
   */
  _showDeleteOrganizationConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' organizations?';
    } else {
      content += 'this selected organization?';
    }

    return this.confirmDialogService.showConfirmDialog('Delete organizations', content, 'Delete');
  }

}
