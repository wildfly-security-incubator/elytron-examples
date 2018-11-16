/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {setIdentityData} from "../actions/actionCreators"
import {getStore} from "../store"

const UNKNOWN = "Unknown error occurred."

export function changePassword(newPassword) {
    return new Promise((resolve, reject) => {
        fetch('/self-service/rest/password/update',
            { method: 'POST', headers: {'Content-Type': 'text/plain'},body: newPassword})
            .then((response) =>  {
                if (response.ok) {
                    resolve("Password successfully changed")
                } else {
                    reject("Cannot change password")
                }
            })
            .catch(response => {
                reject(UNKNOWN)
            })
    })
}

export function updateAttribute(attributeKey, attributeValues) {
    if (!(attributeKey in getStore().getState().identityData.attributes)) {
        return Promise.reject("Cannot update non existing attribute")
    } else {
        return new Promise((resolve, reject) => {
            fetch('/self-service/rest/attribute/update/' + encodeURIComponent(attributeKey),
                { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(attributeValues)})
                .then((response) =>  {
                    if (response.ok) {
                        resolve("Successfully updated")
                    } else {
                        reject("Cannot update attribute")
                    }
                })
                .catch(response => {
                    reject("Cannot update attribute")
                })
        })
    }
}

export function loadData() {
    return new Promise((resolve, reject) => {
        fetch('/self-service/rest/info', {
            method: 'GET'
        })
            .then(response => {
                if (response.ok) {
                    response.json().then(data => {
                        getStore().dispatch(setIdentityData(data))
                        resolve("Data loaded")
                    })
                } else {
                    reject("Cannot load data")
                }
            })
            .catch(response => {
                reject(UNKNOWN)
            })
    })
}
