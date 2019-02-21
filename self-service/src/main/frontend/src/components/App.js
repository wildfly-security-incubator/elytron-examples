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

import React from 'react'
import {Route, Router, Switch} from 'react-router'
import {connect} from 'react-redux'
import {Col} from 'reactstrap'

import {getHistory} from '../history'
import {removeMessage} from "../actions/actionCreators"

import MessageBar from './MessageBar'
import Home from "./Home"
import ChangePassword from './ChangePassword'
import Update from './Update'

const App = ({message, dismissMessage}) => (
    <Router history={getHistory()}>
        <div>
            {
                message ? <MessageBar message={message} dismissMessage={dismissMessage}/> : null
            }
            <Col sm="12" lg={{size: 8, offset: 2}} className='mt-5'>
                <Switch>
                    <Route exact path="/" component={Home}/>
                    <Route path="/password/update" component={ChangePassword}/>
                    <Route path="/attribute/update/:keyToUpdate" component={Update}/>
                </Switch>
            </Col>
        </div>
    </Router>
)

export default connect(
    (state) => ({
        message: state.message,
    }),
    (dispatch) => ({
        dismissMessage: () => dispatch(removeMessage())
    })
)(App)
