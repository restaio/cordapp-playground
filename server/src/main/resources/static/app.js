"use strict";

const PATH_REST_BASE = "/estates";
const PATH_ME = `${PATH_REST_BASE}/me`;
const PATH_PEERS = `${PATH_REST_BASE}/peers`;
const PATH_PURCHASES = `${PATH_REST_BASE}/purchases`;
const PATH_PURCHASE = `${PATH_REST_BASE}/purchase`;
const PATH_STOMP_SUBSCRIBE = "/stomp";
const PATH_STOMP_RESPONSE = "/stompresponse";

const app = angular.module("yoAppModule", ["ui.bootstrap"]);

app.controller("YoAppController", function($scope, $http, $location, $uibModal) {
    const yoApp = this;
    let peers = [];

    // Retrieves my identity.
    (function retrieveMe() {
        $http.get(PATH_ME)
            .then(function storeMe(response) {
                yoApp.me = response.data;
            })
    })();

    // Retrieves a list of network peers.
    (function retrievePeers() {
        $http.get(PATH_PEERS)
            .then(function storePeers(response) {
                peers = response.data.peers;
            })
    })();

    // Starts streaming new Yo's from the websocket.
    (function connectAndStartStreamingYos() {
        let socket = new SockJS(PATH_STOMP_SUBSCRIBE);
        let stompClient = Stomp.over(socket);
        stompClient.connect({}, function startStreamingYos(frame) {
            stompClient.subscribe(PATH_STOMP_RESPONSE, function updateYos(update) {
                let yoState = JSON.parse(update.body);
                yoApp.yos.push(yoState);
                // Forces the view to refresh, showing the new Yo.
                $scope.$apply();
            });
        });
    })();

    // Opens the invest modal.
    yoApp.openPurchaseModal = function openPurchaseModal() {
        $uibModal.open({
            templateUrl: "yoAppModal.html",
            controller: "InvestModalController",
            controllerAs: "investModal",
            resolve: {
                peers: () => peers
            }
        });
    };

    // Gets a list of existing Yo's.
    function purchases() {
        $http.get(PATH_PURCHASES).then(function processYos(response) {
            let yos = Object.keys(response.data)
                .map((key) => response.data[key]);
            yoApp.yos = yos;
        });
    }

    // Pre-populate the list of Yo's.
    purchases();
});

// Controller for invest modal.
app.controller("InvestModalController", function ($http, $location, $uibModalInstance, $uibModal, peers) {
    const modalInstance = this;
    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    // Validates and invest.
    modalInstance.create = function validateAndPurchase() {
        if (isFormInvalid()) {
            modalInstance.formError = true;

        } else {
            modalInstance.formError = false;
            $uibModalInstance.close();

            let purchaseData = $.param({
                target: modalInstance.form.target
            });
            let purchaseHeaders = {
                headers : {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            };

            // Purchase and handles success / fail invest.
            $http.post(PATH_PURCHASE, purchaseData, purchaseHeaders).then(
                modalInstance.displayMessage,
                modalInstance.displayMessage
            );
        }
    };

    // Display result message from purchasing.
    modalInstance.displayMessage = function displayMessage(message) {
        $uibModal.open({
            templateUrl: "messageContent.html",
            controller: "ShowMessageController",
            controllerAs: "showMessageModal",
            resolve: { message: () => message }
        });
    };

    // Closes the puchase modal.
    modalInstance.cancel = $uibModalInstance.dismiss;

    // Validates before purchasing.
    function isFormInvalid() {
        return modalInstance.form.target === undefined;
    }
});

// Controller for success/fail modal dialogue.
app.controller('ShowMessageController', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});

// Intercepts unhandled-rejection errors.
app.config(["$qProvider", function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);