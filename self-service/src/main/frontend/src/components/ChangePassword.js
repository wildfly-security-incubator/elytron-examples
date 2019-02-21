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

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Alert, Button, Col, Form, Row, Input, Label} from 'reactstrap'

import {changePassword, loadData} from "../utils/request"
import {setMessage} from "../actions/actionCreators"
import {nextPath} from "../history"

import "./Home.css"

class ChangePassword extends Component {

    constructor() {
        super()
        this.state = {
            newPassword: "",
            passwordCheck: "",
            changePasswordError: null,
        }
        this.handleSubmit = this.handleSubmit.bind(this)
    }

    checkPasswordsMatch = () => {
        let match = this.state.newPassword === this.state.passwordCheck
        if (!match) {
            this.setState({changePasswordError: "New password does not match the confirmation password"})
        }
        return match
    }

    handleChange(name, event) {
        this.setState({[name]: event.target.value})
    }

    handleSubmit(event) {
        event.preventDefault()
        this.setState({changePasswordError: null})
        if (this.checkPasswordsMatch()) {
            changePassword(this.state.newPassword)
                .then(() => {
                    loadData().then( data => {
                        this.props.setMessage("Password successfully changed")
                        nextPath('/')
                    })
                })
                .catch((response) => {
                    this.props.setMessage(response)
                })
        }
    }

    render() {
        let errorLabel = null
        if (this.state.changePasswordError) {
            errorLabel = (
                <Alert color="danger">
                    {this.state.changePasswordError}
                </Alert>)
        }
        return <Form onSubmit={this.handleSubmit}>
            <Col sm="12" md={{size: 8, offset: 2}} lg={{size: 8, offset: 2}} xl={{size: 6, offset: 3}}>
                <Row className="mb-3">
                    <Label lg={4} for="new-password"> New Password </Label>
                    <Col lg={8}>
                        <Input type="password"
                            className="form-control"
                            id="new-password"
                            value={this.state.newPassword}
                            onChange={this.handleChange.bind(this, "newPassword")}
                            minLength="1"
                            required="required"/>
                    </Col>
                </Row>
                <Row className="mb-3">
                    <Label lg={4} for="confirm-password"> Confirm Password </Label>
                    <Col lg={8}>
                        <Input type="password"
                            className="form-control"
                            id="confirm-password"
                            value={this.state.passwordCheck}
                            onChange={this.handleChange.bind(this, "passwordCheck")}
                            minLength="1"
                            required="required"/>
                    </Col>
                </Row>
                {errorLabel}
                <div className="action-row">
                    <Button type="submit" className="btn btn-primary to-the-right">Change Password</Button>
                </div>
            </Col>
        </Form>
    }
}

export default connect(
    null,
    (dispatch) => ({
        setMessage: (errorMessage) => dispatch(setMessage(errorMessage)),
    })
)(ChangePassword)
