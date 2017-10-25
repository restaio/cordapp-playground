"use strict";

const app = angular.module("yoAppModule", ["ui.bootstrap"]);

// Intercepts unhandled-rejection errors.
app.config(["$qProvider", function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller("YoAppController", function($http, $location, $uibModal) {
    const yoApp = this;
    const apiBasePath = "/yo";

    (function retrieveMe() {
        $http.get(`${apiBasePath}/me`)
            .then(function storeMe(response) {
                yoApp.me = response.data;
            })
    })();

    let peers = [];
    (function retrievePeers() {
        $http.get(`${apiBasePath}/peers`)
            .then(function storePeers(response) {
                peers = response.data.peers;
            })
    })();

    (function connectAndStartStreamingYos() {
        let socket = new SockJS("/stomp");
        let stompClient = Stomp.over(socket);
        stompClient.connect({}, function startStreamingYos(frame) {
            stompClient.send("/stomp/streamYos", {}, "");
            stompClient.subscribe("/stompResponse", getYos);
        });
    })();

    yoApp.openSendYoModal = function openSendYoModal() {
        $uibModal.open({
            templateUrl: "yoAppModal.html",
            controller: "SendYoModalController",
            controllerAs: "sendYoModal",
            resolve: {
                yoApp: () => yoApp,
                apiBasePath: () => apiBasePath,
                peers: () => peers
            }
        });
    };

    function getYos() {
        $http.get(`${apiBasePath}/getYos`)
            .then(function processYos(response) {
                let yos = Object.keys(response.data)
                    .map((key) => response.data[key])
                    .reverse();
                yoApp.yos = yos;
            });
    }

    getYos();
});

app.controller("SendYoModalController", function ($http, $location, $uibModalInstance, $uibModal, yoApp, apiBasePath, peers) {
    const modalInstance = this;
    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    // Validate and send Yo.
    modalInstance.create = () => {
        if (isFormInvalid()) {
            modalInstance.formError = true;

        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            let sendYoEndpoint = `${apiBasePath}/sendYo`;
            let sendYoData = $.param({
                target: modalInstance.form.target
            });
            let sendYoHeaders = {
                headers : {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            };

            // Send Yo and handle success / fail responses.
            $http.post(sendYoEndpoint, sendYoData, sendYoHeaders).then(
                modalInstance.displayMessage,
                modalInstance.displayMessage
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: "messageContent.html",
            controller: "ShowMessageController",
            controllerAs: "showMessageModal",
            resolve: { message: () => message }
        });
    };

    // Close send Yo modal.
    modalInstance.dismissModal = function dismissModal() {
        $uibModalInstance.dismiss();
    }

    // Validates the Yo.
    function isFormInvalid() {
        return modalInstance.form.target === undefined;
    }
});

// Controller for success/fail modal dialogue.
app.controller('ShowMessageController', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});